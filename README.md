# Build Guide API

A REST API for spoiler-free game guides. A guide is a tree of sections (acts, areas, locations, items) of any depth, and readers reveal the content one section at a time as they reach it in the game, so they never see more than they want to.

The same guide can be read in two orders: **story order**, which follows the game and avoids spoilers, and **fastest route**, which puts the most useful items first.

Backend only. Built with Java, Javalin 7, Hibernate 7 (JPA) and PostgreSQL. Game data is imported from the [RAWG](https://rawg.io/apidocs) API.

## Getting started

### Requirements

- Java 17 or newer
- Maven
- PostgreSQL running on `localhost:5432`
- A free RAWG API key from [rawg.io/apidocs](https://rawg.io/apidocs)

### 1. Create the database

```sql
CREATE DATABASE build_guide;
CREATE USER build_guide_user WITH PASSWORD 'choose-a-password';
GRANT ALL PRIVILEGES ON DATABASE build_guide TO build_guide_user;
\c build_guide
GRANT ALL ON SCHEMA public TO build_guide_user;
```

### 2. Add your configuration

Copy `src/main/resources/config.properties.example` to `src/main/resources/config.properties` and fill in the values:

| Key | What it is |
|---|---|
| `DB_NAME` | The database name, e.g. `build_guide` |
| `DB_USERNAME` | The database user |
| `DB_PASSWORD` | The database user's password |
| `SECRET_KEY` | The secret used to sign JWTs. Use a long random string (at least 32 characters) |
| `RAWG_API_KEY` | Your RAWG API key |

`config.properties` is in `.gitignore`, so it is never committed.

### 3. Run it

Run `Main` from IntelliJ, or:

```
mvn clean package
java -jar target/<jar-name>.jar
```

The API starts on **http://localhost:7070/api**. A list of every route is at **http://localhost:7070/api/routes**.

> **Note:** `hibernate.hbm2ddl.auto` is set to `create`, so the database is **emptied on every start**. On startup, an admin user is created:
>
> | Username | Password | Roles |
> |---|---|---|
> | `admin` | `test123` | ADMIN, USER |

### Running deployed

If the environment variable `DEPLOYED` is set, the database connection is read from environment variables instead of `config.properties`: `CONNECTION_STR` (e.g. `jdbc:postgresql://db:5432/`), `DB_NAME`, `DB_USERNAME` and `DB_PASSWORD`.

## Authentication

1. Create a user with `POST /api/users`.
2. Log in with `POST /api/auth/login`. The response contains a JWT.
3. Send the token on every request that needs it:
   ```
   Authorization: Bearer <token>
   ```

Tokens expire after **30 minutes**. There is no logout endpoint, since JWTs are stateless: the client logs out by throwing the token away.

Open routes (`ANYONE`) work without a token. If a token is sent anyway, the user is still recognized, so a logged-in reader gets their own saved reveals.

### Roles

| Role | Can |
|---|---|
| Guest (no token) | Browse published guides and games, and reveal sections. Reveals last only for the session |
| `USER` | Everything a guest can, plus saved reveals, and create and edit their own guides |
| `ADMIN` | Everything, including managing users and roles, importing games, and editing or unpublishing any guide |

## Endpoints

All paths start with `/api`.

### Auth and users

| Method | Path | Role | Description |
|---|---|---|---|
| POST | `/auth/login` | Anyone | Log in and get a JWT |
| GET | `/auth/me` | User | The logged-in user |
| POST | `/users` | Anyone | Create a user |
| GET | `/users` | Admin | All users |
| GET | `/users/{id}` | Admin | One user |
| PUT | `/users/{id}` | User (self) or admin | Update a user |
| DELETE | `/users/{id}` | User (self) or admin | Delete a user and their reveals |
| POST | `/users/{id}/roles/{role}` | Admin | Give a user a role |
| DELETE | `/users/{id}/roles/{role}` | Admin | Remove a role from a user |

### Games

| Method | Path | Role | Description |
|---|---|---|---|
| GET | `/games` | Anyone | All imported games |
| GET | `/games/{id}` | Anyone | One game |
| GET | `/games/{id}/guides` | Anyone | Published guides for a game |
| GET | `/games/search?query=...` | Admin | Search RAWG for games |
| POST | `/games/import/{rawgId}` | Admin | Import a game from RAWG. Gives 409 if it's already imported |

### Guides

| Method | Path | Role | Description |
|---|---|---|---|
| GET | `/guides` | Anyone | All published guides |
| GET | `/guides/mine` | User | The logged-in user's own guides, drafts included |
| POST | `/guides` | User | Create a guide. It starts as a draft |
| GET | `/guides/{id}?mode=STORY\|FASTEST` | Anyone | A guide with its node tree. Drafts give 404 unless you're the author or an admin |
| PUT | `/guides/{id}` | Author or admin | Update a guide |
| DELETE | `/guides/{id}` | Author or admin | Delete a guide with all its nodes and reveals |
| POST | `/guides/{id}/publish` | Author or admin | Publish a guide |
| DELETE | `/guides/{id}/publish` | Author or admin | Make a guide a draft again |

`mode` defaults to `STORY`, which sorts sections by `storyOrderNumber`. `FASTEST` sorts by `priorityNumber`. Switching modes never changes what is revealed.

### Nodes

| Method | Path | Role | Description |
|---|---|---|---|
| POST | `/guides/{guideId}/nodes` | Author or admin | Add a node, at the top level or under `parentId` |
| PUT | `/nodes/{id}` | Author or admin | Update a node. Setting `parentId` moves it with its whole subtree |
| DELETE | `/nodes/{id}` | Author or admin | Delete a node and everything under it |

A node can't be moved under itself or one of its own descendants, or into another guide (400).

### Reveals

| Method | Path | Role | Description |
|---|---|---|---|
| POST | `/nodes/{id}/reveal` | Anyone | Reveal a node and everything under it |
| DELETE | `/nodes/{id}/reveal` | Anyone | Hide a node and everything under it again |
| DELETE | `/guides/{id}/reveals` | Anyone | Reset all reveals for a guide |

Reveals are saved in the database for logged-in users, and kept in the session for guests. Each user's reveals are their own. Content of a node that isn't revealed is sent as `null`, so it never reaches the client before the reader asks for it.

### Errors

Errors are returned as JSON with a status code and a message:

```json
{ "status": 404, "msg": "Could not find the guide" }
```

## How it works

- **Node tree.** `Node` references itself through `parent` and `children`, so a guide can have any structure and depth. There are no fixed levels like act → location → section.
- **Cascading reveals.** Revealing, hiding, moving and deleting a node all work on its whole subtree. `TreeUtil.collectSubtreeIds` loads the guide's nodes in one query and walks the tree in memory, so it works at any depth without one query per level. It's also safe against loops in the data.
- **Drafts.** Guides start unpublished. Unpublished guides are left out of every list, and `GET /guides/{id}` gives 404 rather than 403, so other users can't tell that a draft exists.
- **Layers.** Routes → controllers → services → DAOs. Access rules live in the services; the DAOs only read and write data.

## Testing

### HTTP tests

The `http/` folder has IntelliJ HTTP Client tests with assertions for every endpoint, including the error cases. `Auth.http` sets up users, games and a sample guide, and every other file runs it first.

| File | Covers |
|---|---|
| `Auth.http` | Setup: logins, game imports and a published sample guide with a node tree |
| `Users.http` | Users, login and roles |
| `Guides.http` | Games and guides |
| `Nodes.http` | Creating, updating, moving and deleting nodes |
| `Reveals.http` | Revealing, hiding and resetting, for users and guests |
| `Publish.http` | Drafts, publishing, unpublishing and `/guides/mine` |

Restart the server before a run to start from an empty database, then use **Run All Requests in File**.

### Unit tests

`TreeUtilTest` tests the subtree logic that the cascades depend on: subtrees at any depth, leaves, deep chains, wide trees, list order and loops in the data.

```
mvn test
```

## User stories

The full user stories are in `Build Guide — User Stories.md`.

**Implemented:**

- **Browsing:** list games (B1), guides for a game (B2) and a guide's structure without spoilers (B3)
- **Revealing:** single sections (R1), whole nodes (R2), cascade at any depth (R3), hiding again (R5) and resetting a guide (R7)
- **Ordering:** story order (O1) and fastest route (O2)
- **Accounts:** sign-up (A1), login (A2), saved reveals (A3), guest reveals in the session (A4) and deleting an account (A7)
- **Authoring:** create guides (W1), any tree structure (W2), content (W3), both ordering numbers (W4), edit, move and delete nodes (W6) and publish/unpublish (W7)
- **Admin:** import games from RAWG (M1), manage roles (M2) and unpublish any guide (M3)

**Not implemented, and why:**

| Story | Reason |
|---|---|
| B4, O3, R4 | Belong in a frontend: collapsing branches, a one-time warning and a confirmation dialog |
| B5 Search | Deprioritized. Guides can be browsed per game |
| R6 Progress counts | The `revealed` flag is on every node, so a client can count. No revealed/total counts are calculated in the API |
| O4 Remembered mode | The mode is sent per request with `?mode=` |
| A5 Merge guest reveals on login | Deprioritized |
| A6 Password reset | Needs an email service |
| W5 Spoiler titles, W8 Preview | Deprioritized |
| P1–P3 Interactive map | Planned as a later phase |

**Differences from the stories:**

- There is no separate author role. Any logged-in `USER` can write guides, and only the guide's author or an admin can change it.
- `GET /games` lists every imported game, not only games with at least one published guide (B1).
