# M7 Submission — Views, Generated and Compared

## Group
None. 

## Tools
OpenAI Codex CLI Model: codex-cli 0.170.0

## Other View
`ColoredTerminalView`, launched using the command-line argument `colored`

## AI Generation Runs

**Run 1 — `m7-run-1`**

Prompt: I'm completing M7 Run 1 of my CSC 413 chess project.

Generate a simple, functional Java Swing chess view named `SwingView` that implements my existing `BoardView` interface.

Requirements:
- Display an 8×8 chessboard with clearly visible pieces on both square colors.
- Allow players to select a piece by clicking it, then click a destination square to move.
- Display whose turn it is and the current game status.
- Show messages for illegal moves, check, checkmate, and stalemate.
- Include an Undo button.
- Handle pawn promotion without crashing; defaulting to a queen is acceptable.
- Send all player actions through `GameController.handle()` using `PlayerAction`.
- Read legal moves and game status from `Game`; do not calculate chess rules in the view.
- Implement `start()` as an event-driven Swing window that returns without calling `runLoop()`.
- Update `Main.java` to launch `SwingView` when the command-line argument is `swing`.
- Use only the Java standard library.

Follow AGENTS.md strictly. Do not modify any protected files.

After implementation, run the Maven tests and report the results. Do not commit, tag, push, or switch branches. I will review the changes first.

Result: Generated a Swing chessboard using Unicode chess pieces. The yellow legal-move highlighting made white pieces harder to see.

**Run 2 — `m7-run-2`**

Prompt: I'm completing M7 Run 2 of my CSC 413 chess project.

Generate a simple, functional Java Swing chess view named `SwingView` that implements my existing `BoardView` interface.

Requirements:
- Display an 8×8 chessboard with clearly visible pieces on both square colors.
- Allow players to select a piece by clicking it, then click a destination square to move.
- **When highlighting legal destinations, use a small colored dot or subtle border rather than filling the entire square yellow. The highlighting must not reduce the visibility of white or black pieces.**
- Display whose turn it is and the current game status.
- Show messages for illegal moves, check, checkmate, and stalemate.
- Include an Undo button.
- Handle pawn promotion without crashing; defaulting to a queen is acceptable.
- Send all player actions through `GameController.handle()` using `PlayerAction`.
- Read legal moves and game status from `Game`; do not calculate chess rules in the view.
- Implement `start()` as an event-driven Swing window that returns without calling `runLoop()`.
- Update `Main.java` to launch `SwingView` when the command-line argument is `swing`.
- Use only the Java standard library.

Follow AGENTS.md strictly. Do not modify any protected files.

After implementation, run the Maven tests and report the results. Do not commit, tag, push, or switch branches. I will review the changes first.

Change: Requested dot or border highlighting instead of full yellow squares.

Result: Highlighting improved, but pieces were displayed as oversized letters.

**Run 3 — `m7-run-3`**

Prompt: I'm completing M7 Run 3 of my CSC 413 chess project.

Generate a simple, functional Java Swing chess view named `SwingView` that implements my existing `BoardView` interface.

Requirements:
- Display an 8×8 chessboard with clearly visible pieces on both square colors.
- Allow players to select a piece by clicking it, then click a destination square to move.
- **When highlighting legal destinations, use a small colored dot or subtle border rather than filling the entire square yellow. The highlighting must not reduce the visibility of white or black pieces.**
- **Display pieces using Unicode chess-piece symbols, not letters. Ensure the symbols are clearly visible and distinguishable on both square colors.**
- Display whose turn it is and the current game status.
- Show messages for illegal moves, check, checkmate, and stalemate.
- Include an Undo button.
- Handle pawn promotion without crashing; defaulting to a queen is acceptable.
- Send all player actions through `GameController.handle()` using `PlayerAction`.
- Read legal moves and game status from `Game`; do not calculate chess rules in the view.
- Implement `start()` as an event-driven Swing window that returns without calling `runLoop()`.
- Update `Main.java` to launch `SwingView` when the command-line argument is `swing`.
- Use only the Java standard library.

Follow AGENTS.md strictly. Do not modify any protected files.

After implementation, run the Maven tests and report the results. Do not commit, tag, push, or switch branches. I will review the changes first.

Change: Explicitly required Unicode chess symbols instead of letters.

Result: Good visibility, contrasting outlines, and dot/border highlighting. Selected this implementation.

## AGENTS.md
I adapted the Session 17 instructions to my project, including the rule that a modal dialog asking one question is permitted, but Swing must never call `controller.runLoop()`.

## Code Review
### 1. Mouse Clicks:`SwingView.java`, line 143

- **What:** Handles piece selection and sends moves to `GameController`.
- **Checked:** Tested selecting and moving pieces, and verified the controller handles moves.
- **Decision:** Kept. Works correctly and follows MVC.

### 2. Legal-Move Highlighting: `SwingView.java`, line 269

- **What:** Uses `game.legalMoves()` to highlight valid destinations with dots.
- **Checked:** Selected different pieces and compared highlighting with Run 1.
- **Decision:** Kept. Clearer highlighting without hiding pieces.

### 3. Drawing Pieces: `SwingView.java`, line 290

- **What:** Draws Unicode chess pieces with contrasting outlines.
- **Checked:** Compared piece visibility across Runs 2 and 3.
- **Decision:** Kept. Symbols are easier to recognize and see on both square colors.

## Comparison

**1. Selected Square**
- **Runs 1–3:** Each allowed clicking a piece and then its destination.
- **Run 3:** Stores the selected square in a `Position` variable (`SwingView.java`, line 49).

**2. Legal Moves**
- **Runs 1–3:** All supported legal-move highlighting.
- **Run 1:** Yellow highlights made white pieces harder to see.
- **Runs 2–3:** Dots provided better visibility.

**3. Promotion**
- **Runs 1–3:** All defaulted to queen promotion.
- **Run 3:** Uses `promotionChoice()` (`SwingView.java`, lines 78–80).

**4. Final Decision**

I kept **Run 3** because it combined the best features: recognizable Unicode chess pieces, contrasting outlines, and clear legal-move highlighting. It was also easier to use visually without adding unnecessary complexity.

## Applied Idea
The first run met all requirements except that when selecting a piece, the highlight functionality that codex implemented was making the white pieces invisible. For this reason, during run 2 I asked codex to follow a style more similar to chess.com with dots to highlight possible moves.

## Debugging Evidence
The colored terminal initially displayed chess pieces as question marks. Running Java with UTF-8 encoding corrected the symbols. This wasn't necessary when running the colored terminal view inside IntelliJ.

I also investigated a pawn-color rendering issue. The black pawns appeared purple despite the ANSI foreground color. I tested a Unicode text-presentation selector, but the issue persisted. I also tried changing the 

## Manual Change
Commit: "M7 by hand: adjust terminal colors for readability"

I manually changed the color that `drawHighlights` function sets the highlighted dot of possible moves to in `SwingView.java`. It was an ugly green color that was hard to see on the beige board. I changed it manually to purple after locating how codex implemented the dot highlight functionality.

## Verification
- Maven tests: 83 passed, 0 failures, 0 errors.
- Swing view tested manually.
- Colored terminal view tested manually.
- Protected project files unchanged (confirmed by codex and git status)