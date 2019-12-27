/**
 * Class Maze creates the Maze and finds the solution
 * @author nicreid
 */
public class Maze {
    /**
     * previousX stores the previous X location of the traversal
     */
    private static int previousX;

    /**
     * previousY stores the previous Y location of the traversal
     */
    private static int previousY;


    /**
     * Main creates the two-dimensional maze and then calls the traversal method
     * @param args
     */
    public static void main(String[] args){
        char maze[][] =
                {       { '#', '#', '#', '#', '#', '#', '#', '#', '#', '#', '#', '#' },
                        { '#', '.', '.', '.', '#', '.', '.', '.', '.', '.', '.', '#' },
                        { '.', '.', '#', '.', '#', '.', '#', '#', '#', '#', '.', '#' },
                        { '#', '#', '#', '.', '#', '.', '.', '.', '.', '#', '.', '#' },
                        { '#', '.', '.', '.', '.', '#', '#', '#', '.', '#', '.', '.' },
                        { '#', '#', '#', '#', '.', '#', '.', '#', '.', '#', '.', '#' },
                        { '#', '.', '.', '#', '.', '#', '.', '#', '.', '#', '.', '#' },
                        { '#', '#', '.', '#', '.', '#', '.', '#', '.', '#', '.', '#' },
                        { '#', '.', '.', '.', '.', '.', '.', '.', '.', '#', '.', '#' },
                        { '#', '#', '#', '#', '#', '#', '.', '#', '#', '#', '.', '#' },
                        { '#', '.', '.', '.', '.', '.', '.', '#', '.', '.', '.', '#' },
                        { '#', '#', '#', '#', '#', '#', '#', '#', '#', '#', '#', '#' } };
        MazeTraversal(maze, 2, 0);
    }

    /**
     * printMaze outputs an image of the maze in the console
     * @param maze
     */
    public static void printMaze(char[][] maze){
        for(int i = 0; i < 12; i++){
            System.out.println();
            for(int j = 0; j < 12; j++){
                System.out.print(maze[i][j] + " ");
            }
        }
        System.out.println("\n\n-----------------------");
    }

    /**
     * MazeTraversal passes through each step of the maze, finding the correct solution to get through
     * @param maze
     * @param locationY
     * @param locationX
     */
    public static void MazeTraversal(char[][] maze, int locationY, int locationX){
        if(locationY != 0 && locationX != 11 && locationY != 11) {
            int currentY = locationY;
            int currentX = locationX;
            if (maze[locationY + 1][locationX] == '.' && locationY+1 != previousY) {
                maze[locationY][locationX] = '0';
                currentY += 1;
            } else if (maze[locationY][locationX + 1] == '.' && locationX+1 != previousX) {
                maze[locationY][locationX] = '0';
                currentX += 1;
            } else if (maze[locationY - 1][locationX] == '.' && locationY-1 != previousY) {
                maze[locationY][locationX] = '0';
                currentY -= 1;
            } else if (maze[locationY][locationX - 1] == '.' && locationX-1 != previousX) {
                maze[locationY][locationX] = '0';
                currentX -= 1;
            } else if (maze[locationY + 1][locationX] == '0') {
                maze[locationY + 1][locationX] = '.';
                currentY += 1;
            } else if (maze[locationY][locationX - 1] == '0') {
                maze[locationY][locationX - 1] = '.';
                currentX -= 1;
            } else if (maze[locationY - 1][locationX] == '0') {
                maze[locationY - 1][locationX] = '.';
                currentY -= 1;
            } else if (maze[locationY][locationX + 1] == '0') {
                maze[locationY][locationX + 1] = '.';
                currentX += 1;
            }
            previousX = locationX;
            previousY = locationY;
            printMaze(maze);
            MazeTraversal(maze, currentY, currentX);
        }
        else{
            maze[locationY][locationX] = '0';
            printMaze(maze);
        }

    }
}
