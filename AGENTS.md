# Instructions for coding agents

This is a Java chess engine built using MVC.
The model and controller are complete and written by hand.
Your job in M7 is to create new views only.

## Do

- Add new views in `src/main/java/edu/sfsu/csc413/chess/view/`.
- Implement the existing `BoardView` interface.
- Read `BoardView`, `PlayerAction`, `ConsoleView`, and `GameController` before writing code.
- Read game state through `Game`: `board()`, `sideToMove()`, `legalMoves()`, `status()`, and `history()`.
- Send player actions through `controller.handle(new PlayerAction.Move("e2e4"))` or the corresponding action record.
- Update `Main.java` to select the new views using command-line arguments.
- Use only Java's standard library.

## Don't

- Don't modify anything in `model/`, `engine/`, or `factory/`.
- Don't modify `GameController`, `BoardView`, `PlayerAction`, `ConsoleView`, or `EmojiView`.
- Don't modify `pom.xml` or existing tests.
- Don't call `game.play()`, `game.undoLastMove()`, or `Board.apply()` from a view.
- Don't calculate chess rules inside a view. Use `game.legalMoves()` and `game.status()`.
- Don't wait for the next move inside a Swing window.
- Swing's `start()` must create the window and return; it must never call `controller.runLoop()`.
- A modal dialog that asks one question, such as which piece to promote to, is allowed.
- Don't introduce external dependencies.

## Check your work

- Run `./mvnw test`. All 83 existing tests must pass.
- Do not modify, delete, or disable existing tests.
- Check `git diff --stat` for unexpected changes.
- Verify that the new view does not modify game state directly.
- Manually test a legal move, an illegal move, Undo, and Fool's Mate.
- Verify the Swing window opens and remains responsive.

## Stop and ask

If a task requires changing any protected file, stop and explain why instead of modifying it.
```
