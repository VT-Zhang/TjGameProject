# Conway's Game of Life Implementation Guide

## Overall Description

This package should implement a colorful version of Conway's Game of Life using Java Swing.

Unlike the classic "infinite plane" explanation, this project should use a fixed `32 x 32` board. The board still behaves continuously because it wraps around at the edges:

- moving past the left edge enters from the right edge
- moving past the right edge enters from the left edge
- moving past the top edge enters from the bottom edge
- moving past the bottom edge enters from the top edge

The design should use multiple classes, with each class handling one clear responsibility. Avoid putting window setup, simulation rules, rendering, input handling, and board state into one large class.

## Target Behavior

- Display a `32 x 32` grid of cells
- Each cell is either alive or dead
- The simulation updates on a timer
- The user can start, pause, clear, randomize, and restart the board
- The board is colorful instead of plain black-and-white
- Edge wrapping must be enabled for neighbor checks

## Recommended Class Design

### `Main`

Responsibility:

- Start the application on the Swing event dispatch thread

Expected behavior:

- Call `SwingUtilities.invokeLater`
- Create the main frame
- Show the frame

### `GameOfLifeFrame`

Responsibility:

- Configure the main window
- Assemble the main panel and optional control bar

Expected behavior:

- Set the title
- Set the close operation
- Add the main game UI
- Pack the frame
- Center the frame on screen
- Keep the window size stable

### `GameOfLifeBoard`

Responsibility:

- Store the current generation of cells
- Store the next generation when stepping the simulation
- Expose board-specific helpers

Important state:

- `ROWS = 32`
- `COLS = 32`
- `boolean[][] currentCells`
- `boolean[][] nextCells`

Expected methods:

- `isAlive(int row, int col)`
- `setAlive(int row, int col, boolean alive)`
- `clear()`
- `randomize(Random random)`
- `swapGenerations()`
- `getWrappedRow(int row)`
- `getWrappedCol(int col)`

Why this class matters:

- It keeps cell data isolated from UI code.
- It provides one place to enforce wrap-around behavior.

### `LifeRules`

Responsibility:

- Apply Conway's rules
- Count live neighbors using wrap-around coordinates

Conway's rules:

- A live cell with fewer than `2` live neighbors dies
- A live cell with `2` or `3` live neighbors lives
- A live cell with more than `3` live neighbors dies
- A dead cell with exactly `3` live neighbors becomes alive

Expected methods:

- `countLiveNeighbors(GameOfLifeBoard board, int row, int col)`
- `computeNextState(GameOfLifeBoard board, int row, int col)`
- `step(GameOfLifeBoard board)`

Why this class matters:

- It separates simulation logic from drawing and event handling.

### `GameOfLifePanel`

Responsibility:

- Render the board
- Handle mouse input for toggling cells
- Handle the timer-driven animation loop
- Coordinate button actions such as start, pause, clear, and randomize

Important state:

- reference to `GameOfLifeBoard`
- reference to `LifeRules`
- Swing `Timer`
- `boolean running`
- cell size
- current color mode or palette

Expected behavior:

- Draw the full `32 x 32` board
- Paint alive cells in bright colors
- Paint dead cells with a darker background
- Draw visible grid lines
- Advance the simulation on each timer tick when running
- Repaint after every update
- Allow clicking or dragging to turn cells on or off before or during a pause

### `CellPalette`

Responsibility:

- Provide colors for the board
- Keep color choices out of the simulation logic

Possible design:

- background color
- grid color
- alive cell color
- alternate alive color
- highlight color for recently born cells

Optional enhancement:

- Color cells differently based on age if you decide to track cell age later

Why this class matters:

- It makes the game colorful without polluting the logic classes with UI color constants.

### `ControlPanel` (optional)

Responsibility:

- Hold UI controls such as buttons and speed controls

Possible controls:

- `Start`
- `Pause`
- `Step`
- `Clear`
- `Randomize`
- speed slider

Why this class can help:

- It keeps button layout separate from board rendering.

## Suggested Data Flow

1. `Main` starts the program
2. `GameOfLifeFrame` creates the UI
3. `GameOfLifePanel` owns a `GameOfLifeBoard` and a `LifeRules` instance
4. The timer fires repeatedly
5. `LifeRules.step(...)` computes the next generation
6. `GameOfLifeBoard` swaps the current and next arrays
7. `GameOfLifePanel` repaints the board

## Crucial Logic

## Wrap-Around Coordinates

Purpose:

- Make the `32 x 32` board behave like a looped surface instead of a hard wall

Pseudocode:

- wrapped row = `(row + ROWS) % ROWS`
- wrapped col = `(col + COLS) % COLS`

Use this whenever reading neighbors.

Example:

- row `-1` becomes `31`
- row `32` becomes `0`
- col `-1` becomes `31`
- col `32` becomes `0`

## Neighbor Counting

Purpose:

- Count all eight surrounding cells correctly, even on edges and corners

Pseudocode:

- set count to `0`
- for `dr` from `-1` to `1`
  - for `dc` from `-1` to `1`
    - skip when `dr == 0` and `dc == 0`
    - wrappedRow = board wrapped row of `row + dr`
    - wrappedCol = board wrapped col of `col + dc`
    - if the wrapped neighbor cell is alive:
      - increase count

Why this matters:

- This is the core of the toroidal board behavior.

## Stepping One Generation

Purpose:

- Compute the next board state without corrupting the current state during the calculation

Pseudocode:

- for each row in `0..31`
  - for each col in `0..31`
    - count live neighbors
    - compute whether this cell should be alive next
    - write the result into `nextCells[row][col]`
- swap `currentCells` and `nextCells`

Why this matters:

- Never update cells in place while you are still reading neighbors from the same generation.

## Rendering Notes

The board should feel colorful and readable.

Recommended rendering order:

1. panel background
2. dead-cell board background
3. alive cells
4. grid lines
5. status text if needed

Color suggestions:

- dark navy or charcoal background
- muted grid lines
- bright green, cyan, orange, or pink alive cells
- optional accent color for new births

Keep the palette high-contrast enough that the board is easy to scan.

## Interaction Notes

Recommended mouse behavior:

- click a dead cell to make it alive
- click an alive cell to make it dead
- drag to paint living cells

Recommended controls:

- `Start` begins continuous updates
- `Pause` stops the timer
- `Step` advances exactly one generation
- `Clear` kills all cells
- `Randomize` fills the board with a random pattern

## Suggested Implementation Order

1. Create `Main`
2. Create `GameOfLifeFrame`
3. Create `GameOfLifeBoard`
4. Create `LifeRules`
5. Create `GameOfLifePanel`
6. Add timer-based stepping
7. Add mouse editing
8. Add colors through `CellPalette`
9. Add optional controls and speed adjustment

## What Not To Do

- Do not hard-code logic and rendering into one giant panel class
- Do not treat off-screen neighbors as dead; they must wrap around
- Do not update the current board in place while calculating the next generation
- Do not use an unbounded or scrolling board for this assignment
- Do not make the board monochrome if the requirement is to be colorful

## Minimal Example Responsibility Split

- `Main`: launch
- `GameOfLifeFrame`: window
- `GameOfLifeBoard`: cell storage
- `LifeRules`: Conway logic and wrap-around neighbor counting
- `GameOfLifePanel`: drawing, timer, mouse input
- `CellPalette`: colors

That split is small enough to implement comfortably and still satisfies the requirement that each class is responsible for its own work.
