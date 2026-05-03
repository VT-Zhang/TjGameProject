# Snake Game Implementation Guide

## Overall Description

This game is a classic grid-based Snake game built with Java Swing.

The player controls a snake that moves one tile at a time across a fixed board. Food appears at random empty tiles. Each time the snake eats the food, the score increases and the snake grows longer. The game ends if the snake hits the wall or runs into its own body.

The implementation in this package is structured as a small Swing application:

- `Main` starts the program on the Swing event thread.
- `SnakeFrame` creates the window and places the game panel inside it.
- `SnakePanel` owns the game state, drawing, keyboard input, and timer-based game loop.

## How to Play

- Use the arrow keys to change the snake's direction.
- The snake keeps moving automatically once the game starts.
- Eat the food to grow longer and increase the score.
- Avoid hitting the borders of the board.
- Avoid colliding with the snake's own body.
- When the game ends, press `R` to restart.

## Classes To Create

### `Main`

Responsibility:

- Start the application.
- Create the game window on the Swing event dispatch thread.

Plain-English behavior:

- Launch the frame through Swing utilities so UI work runs on the proper thread.
- Show the frame after it is created.

### `SnakeFrame`

Responsibility:

- Configure the main application window.
- Add the game panel.

Plain-English behavior:

- Set the window title.
- Set the close operation.
- Prevent resizing so the board dimensions stay stable.
- Add one `SnakePanel`.
- Pack the frame so it uses the panel's preferred size.
- Center the window on screen.

### `SnakePanel`

Responsibility:

- Store the entire game state.
- Run the update loop with a Swing timer.
- Listen for keyboard input.
- Draw the board, snake, food, score, and game-over overlay.

Important state this class should manage:

- Tile size
- Grid width and height
- Panel width and height
- Starting snake length
- Timer delay
- Random number generator
- Timer
- A list of snake body segments
- Food position
- Current direction
- Next direction
- Running or game-over state
- Score

### `Direction`

Responsibility:

- Represent movement directions: up, down, left, right.
- Support checking whether two directions are opposites.

This can be a nested enum inside `SnakePanel`, which matches the current package design.

## Game Flow

The game should follow this lifecycle:

1. The program starts and opens the frame.
2. The panel initializes its board settings and input handling.
3. The game state is reset in a start method.
4. The timer fires repeatedly.
5. On each timer tick:
   - move the snake
   - check whether food was eaten
   - check whether the game should end
   - repaint the panel
6. If the snake dies, stop the timer and show the restart message.
7. If the player presses `R`, reset everything and start again.

## Crucial Methods And Their Logic

## `startGame`

Purpose:

- Reset the game to a clean starting state.

Pseudocode:

- Clear the snake body list
- Pick a starting head position near the center of the grid
- Add body segments extending left from the head until the starting length is reached
- Set both current direction and next direction to right
- Reset score to zero
- Mark the game as running
- Spawn food at a valid empty tile
- Start the timer
- Request keyboard focus for the panel

Why this matters:

- This method guarantees restart behavior is consistent and predictable.

## `spawnFood`

Purpose:

- Place food on a random tile that is not occupied by the snake.

Pseudocode:

- Repeat:
  - pick a random x coordinate within the grid
  - pick a random y coordinate within the grid
  - create a candidate point from those coordinates
- Continue repeating while the snake already contains that point
- Store the accepted point as the food location

Why this matters:

- Food must never appear inside the snake body.

## `actionPerformed`

Purpose:

- Act as the timer callback for the game loop.

Pseudocode:

- If the game is running:
  - move the snake
  - check whether the snake has eaten food
  - check whether the snake has collided with anything fatal
- Repaint the panel

Why this matters:

- This is the heartbeat of the game. It keeps all updates centralized and ordered.

## `moveSnake`

Purpose:

- Advance the snake by one tile.

Pseudocode:

- Copy next direction into current direction
- Read the current head segment
- Create a new head position based on the current head
- Adjust the new head coordinates:
  - if moving up, decrease y
  - if moving down, increase y
  - if moving left, decrease x
  - if moving right, increase x
- Insert the new head at the front of the snake list
- If the new head is not on the food tile:
  - remove the last segment of the snake

Why this matters:

- Growth happens naturally by skipping tail removal when food is eaten.

## `checkFood`

Purpose:

- Detect whether the snake has eaten the current food.

Pseudocode:

- If the snake head equals the food location:
  - increase the score
  - spawn a new food item

Why this matters:

- The game should only reward the player when the head reaches the food tile.

## `checkCollisions`

Purpose:

- End the game if the snake hits the wall or itself.

Pseudocode:

- Read the head position
- If head x is less than zero or greater than or equal to grid width:
  - end the game
  - stop checking further
- If head y is less than zero or greater than or equal to grid height:
  - end the game
  - stop checking further
- For each body segment after the head:
  - if the head equals that segment:
    - end the game
    - stop checking further

Why this matters:

- Collision checks should happen after movement so the new head position is evaluated correctly.

## `endGame`

Purpose:

- Stop active gameplay and freeze the final state.

Pseudocode:

- Mark the game as not running
- Stop the timer

Why this matters:

- Once the player loses, the game should stop updating immediately.

## `updateDirection`

Purpose:

- Accept player input without allowing an instant reversal into the snake's body.

Pseudocode:

- If the candidate direction is not opposite to the current direction:
  - save it as the next direction

Why this matters:

- Without this rule, the player could reverse directly into the snake's own neck and create awkward behavior.

## Keyboard Handling

Responsibility:

- Convert key presses into game actions.

Expected behavior:

- Up arrow requests upward movement
- Down arrow requests downward movement
- Left arrow requests left movement
- Right arrow requests right movement
- `R` restarts the game only when the game is over

Pseudocode:

- When a key is pressed:
  - if it is an arrow key:
    - call the direction update method with the corresponding direction
  - if it is `R` and the game is not running:
    - call the start method

## Rendering Responsibilities

The panel should draw these parts in order:

1. Background
2. Grid
3. Food
4. Snake
5. Score
6. Game-over overlay when needed

### `paintComponent`

Purpose:

- Render the current game state every frame.

Pseudocode:

- Call the parent paint method first
- Draw the grid
- Draw the food
- Draw the snake
- Draw the score
- If the game is over:
  - draw the game-over overlay and restart prompt

Why this matters:

- Rendering order controls what appears on top of what.

## Design Notes

- Keep board dimensions fixed so movement stays aligned with the grid.
- Store the snake as ordered body segments, with index `0` as the head.
- Separate current direction from next direction so key presses between timer ticks are handled cleanly.
- Use a Swing `Timer` instead of a manual loop so the UI remains responsive.
- Keep restart logic inside the panel because the panel owns the game state.

## Suggested Implementation Order

1. Create `Main`
2. Create `SnakeFrame`
3. Create `SnakePanel` with constants and fields
4. Add the start/reset logic
5. Add timer updates
6. Add snake movement
7. Add food spawning and scoring
8. Add collision detection
9. Add keyboard input
10. Add rendering and game-over overlay

## What Not To Do

- Do not place food without checking whether the tile is already occupied.
- Do not allow the snake to reverse directly into the opposite direction.
- Do not update the snake after the game has ended.
- Do not mix drawing logic with random state resets.
- Do not depend on free-form pixel movement; this game should remain tile-based.
