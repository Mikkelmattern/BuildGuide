# Build Guide — User Stories

Oct 12, 2026 · @Mikkel Fuglsig Nielsen

## Roles and conventions


| Role | Who it is | Can do |
| --- | --- | --- |
| Guest | Anyone not logged in | Browse guides, reveal sections; progress lasts only for the session |
| Reader | Logged-in user | Everything a guest can, plus saved reveal progress across devices |
| Author | Reader with author rights | Create and edit guides, the node tree and section numbers |
| Admin | Site owner (Mikkel) | Manage users, roles, games and published guides |

Each story follows *As a \[role\], I want \[goal\], so that \[benefit\]*, with acceptance criteria written to be testable in JUnit or by hand.

## Epic 1: Browsing guides

Readers find a guide and see its structure without seeing any spoilers.

| ID | User story | Acceptance criteria | Priority |
| --- | --- | --- | --- |
| B1 | As a guest, I want to see a list of games with guides, so that I can find the game I'm playing. | Front page lists games with at least one published guide; draft guides are not shown. | Must |
| B2 | As a guest, I want to see the guides for a game, so that I can pick a build or route. | Each guide shows title, author and last-updated date; clicking opens the guide. | Must |
| B3 | As a guest, I want to see a guide's structure (acts, locations, sections) without content, so that I know what it covers without spoilers. | Node titles are visible; section content is hidden until revealed. Titles marked as spoilers show a placeholder instead. | Must |
| B4 | As a guest, I want to expand and collapse branches of the tree, so that long guides stay manageable. | Each node with children can be toggled; collapse state does not affect reveal state. | Should |
| B5 | As a guest, I want to search guides by game and title, so that I can find one quickly. | Search matches game name and guide title, case-insensitive. Search never matches hidden section content. | Could |

## Epic 2: Revealing content

Content appears only after an explicit reader action, at the granularity the reader chooses. This is the core of the product.

| ID | User story | Acceptance criteria | Priority |
| --- | --- | --- | --- |
| R1 | As a reader, I want to reveal a single section with a button, so that I only see what I've reached in-game. | Clicking "I've reached this" shows that section's content. No content is sent to the browser before the click. | Must |
| R2 | As a reader, I want to reveal a whole node (e.g. a location), so that all items there show at once. | Revealing a node reveals all its descendants. Siblings and parent nodes stay hidden. | Must |
| R3 | As a reader, I want reaching a milestone (e.g. a new act) to cascade to everything nested under it, so that I don't click every section. | Cascade covers all depths of the subtree, whatever the tree's shape. Works for any game's structure. | Must |
| R4 | As a reader, I want a confirmation before a large cascade, so that I don't spoil a lot by misclick. | If a reveal would show more than N sections (configurable, e.g. 10), a confirm dialog shows the count. | Should |
| R5 | As a reader, I want to hide a section again, so that I can undo a mistaken reveal. | "Hide again" re-hides that node and its subtree in my state only. | Should |
| R6 | As a reader, I want to see which parts I've revealed, so that I can track my progress. | Revealed nodes are visually marked; each branch shows revealed / total sections. | Should |
| R7 | As a reader, I want to reset all progress for a guide, so that I can start a new playthrough. | Reset asks for confirmation and clears all reveals for that guide only. | Could |

## Epic 3: Dual ordering

The same content serves two reading modes by sorting on a different field: `priorityNumber` or `storyOrderNumber`.

| ID | User story | Acceptance criteria | Priority |
| --- | --- | --- | --- |
| O1 | As a reader, I want to read a guide in story order, so that I meet content as the game presents it and stay spoiler-safe. | Story mode sorts sections by `storyOrderNumber`. This is the default mode. | Must |
| O2 | As a reader, I want to switch to "fastest route" order, so that I get the best items first regardless of story. | Fastest mode sorts by `priorityNumber`. Switching modes never changes reveal state. | Must |
| O3 | As a reader, I want a warning when switching to fastest route, so that I know it may skip ahead in the story. | First switch per guide shows a one-time notice. | Should |
| O4 | As a reader, I want my chosen mode remembered, so that I don't switch every visit. | Logged-in: saved per guide. Guest: kept for the session. | Could |

## Epic 4: Accounts and saved progress

Logged-in readers keep their reveal state; guests keep it only for the session. Auth is new ground compared to earlier school projects, so these stories are split small.

| ID | User story | Acceptance criteria | Priority |
| --- | --- | --- | --- |
| A1 | As a guest, I want to create an account, so that my progress is saved. | Username + email + password; email and username unique; password stored hashed (e.g. BCrypt), never plain text. | Must |
| A2 | As a reader, I want to log in and out, so that I can use my account securely. | Valid login issues a session/token; wrong credentials give a generic error; logout invalidates it. | Must |
| A3 | As a reader, I want my reveals saved, so that I can continue where I left off on any device. | Each reveal is persisted per user + node; reloading or logging in elsewhere shows the same state. | Must |
| A4 | As a guest, I want reveals to work during my visit, so that I can use the site without an account. | Guest reveals are stored in the session only and are gone when it ends. Nothing is written to the database. | Must |
| A5 | As a guest, I want my session progress kept when I sign up, so that I don't redo my reveals. | On signup/login in the same session, guest reveals merge into the account. | Should |
| A6 | As a reader, I want to reset my password, so that I can get back in if I forget it. | Reset link by email, single use, expires after a set time. | Could |
| A7 | As a reader, I want to delete my account, so that my data is removed. | Deletes the user and all their reveal state after confirmation. | Should |

## Epic 5: Authoring guides

Authors build the guide as a tree of self-referencing `Node`s of any depth and set both ordering numbers per section.

| ID | User story | Acceptance criteria | Priority |
| --- | --- | --- | --- |
| W1 | As an author, I want to create a guide for a game, so that I can start writing. | Guide has title, game and description; starts as a draft visible only to its author. | Must |
| W2 | As an author, I want to add child nodes under any node, so that I can model the game's own structure (acts, regions, locations). | A node can have any number of children at any depth; no fixed Act → Location → Section levels. | Must |
| W3 | As an author, I want to write content for a section, so that readers get the actual guide info. | Section holds title + body text (Markdown or rich text); saved to the database. | Must |
| W4 | As an author, I want to set `priorityNumber` and `storyOrderNumber` per section, so that both reading modes sort correctly. | Both fields are editable; storyOrderNumber is pre-filled from the node's position in the tree and can be overridden. | Must |
| W5 | As an author, I want to mark a node title itself as a spoiler, so that even the structure doesn't give things away. | Flagged titles show a placeholder (e.g. "Hidden location") until revealed. | Should |
| W6 | As an author, I want to edit, move and delete nodes, so that I can restructure the guide. | Moving a node moves its subtree; deleting asks for confirmation and removes the subtree. | Must |
| W7 | As an author, I want to publish or unpublish a guide, so that readers only see finished work. | Only published guides appear to guests and readers. | Must |
| W8 | As an author, I want to preview the guide as a reader, so that I can check order and reveals. | Preview uses a throwaway reveal state that doesn't touch real users. | Could |

## Epic 6: Administration

A small admin surface so the site can be run without touching the database by hand.

| ID | User story | Acceptance criteria | Priority |
| --- | --- | --- | --- |
| M1 | As an admin, I want to add games, so that authors can write guides for them. | Game has name and optional cover image; only admins can create games. | Must |
| M2 | As an admin, I want to grant and remove author rights, so that I control who writes guides. | Role change takes effect on the user's next request. | Should |
| M3 | As an admin, I want to unpublish any guide, so that I can remove broken or inappropriate content. | Unpublished guides disappear for readers; reader progress is kept. | Should |

## Later phase: Interactive map

Not MVP. An optional per-game add-on built on an original map image with Leaflet, since an embedded third-party map can't be tied to reveal logic.

| ID | User story | Acceptance criteria | Priority |
| --- | --- | --- | --- |
| P1 | As a reader, I want to see a game map with pins for guide items, so that I know where things are. | Map supports pan and zoom; pins are linked to nodes. | Won't for now |
| P2 | As a reader, I want unrevealed pins to show only as vague markers, so that the map doesn't spoil anything. | Unrevealed pin = generic icon, no title or details; revealing the node shows full detail. Reveal state is shared with the list view. | Won't for now |
| P3 | As an author, I want to place a node's pin on the map, so that items show in the right spot. | Author clicks the map to set coordinates on a node; can move or remove the pin. | Won't for now |

Open question: the rights to use a map image differ per game and need checking before P1 starts.

## MVP and suggested order

The MVP is the 19 "Must" stories, for one game, with the generic Node tree in place from day one. Built in this order, each step gives something you can demo:

1. **Data model and authoring base** — W1, W2, W3, W4, M1. Node entity, DAOs and tests for the tree first; this is expensive to change later.
2. **Read-only browsing** — B1, B2, B3, O1. Seed one game's guide and show its structure.
3. **Reveal mechanics for guests** — R1, R2, R3, A4, O2. Session-only reveals, cascade and both sort modes.
4. **Accounts and saved progress** — A1, A2, A3. The new auth territory, kept separate so it doesn't block the core.
5. **Finish authoring** — W6, W7. Restructure and publish.
6. **Should/Could stories** as time allows, then the map phase.

Cascade (R3) and the `storyOrderNumber` default (W4) are the stories most worth writing JUnit tests for first.
