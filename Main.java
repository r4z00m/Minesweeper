import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Random;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        try (Scanner scanner = new Scanner(System.in)) {
            System.out.print("How many mines do you want on the field? ");
            int minesCount;
            try {
                minesCount = Integer.parseInt(scanner.nextLine());
            } catch (Exception e) {
                System.out.println("Error! Put number!");
                return;
            }
            if (minesCount < 1 || minesCount > 80) {
                System.out.println("Error! Wrong number of mines!");
                return;
            }
            Game game = new Game(minesCount);
            game.printFiled(false);
            game.startGame();
        }
    }
}

class Game {

    private final int size = 11;
    private final int minesCount;
    private Ceil[][] map;

    public Game(int minesCount) {
        this.minesCount = minesCount;
        initMap();
    }

    public void startGame() {
        try (Scanner scanner = new Scanner(System.in)) {
            boolean gameStarted = true;
            while (true) {
                System.out.print("Set/unset mine marks or claim a cell as free: ");
                String[] strings;
                try {
                    strings = scanner.nextLine().split(" ");
                } catch (Exception e) {
                    System.out.println("Error! Fatal!");
                    break;
                }
                if (strings.length != 3) {
                    System.out.println("Error! Wrong count arg! Try again.");
                    continue;
                }
                int x = initCoordinate(strings[0]);
                if (x < 0) {
                    continue;
                }
                int y = initCoordinate(strings[1]);
                if (y < 0) {
                    continue;
                }
                if (strings[2].equals("free")) {
                    if (map[y][x].isMarked()) {
                        System.out.println("Error! Can't free marked! Try again.");
                        continue;
                    }
                    if (map[y][x].getValue() == 'X') {
                        if (gameStarted) {
                            while (map[y][x].getValue() != '/') {
                                initMap();
                            }
                            openNearFreeCells(y, x);
                        } else {
                            map[y][x].setChecked(true);
                            printFiled(true);
                            System.out.println("You stepped on a mine and failed!");
                            break;
                        }
                    } else if (map[y][x].getValue() == '/') {
                        openNearFreeCells(y, x);
                    } else {
                        if (!map[y][x].isChecked()) {
                            map[y][x].setChecked(true);
                        }
                    }
                } else if (strings[2].equals("mine")) {
                    if (map[y][x].isChecked()) {
                        System.out.println("Error! The ceil is free! Try again.");
                        continue;
                    }
                    map[y][x].setMarked(!map[y][x].isMarked());
                } else {
                    System.out.println("Error! Wrong command! Try again.");
                    continue;
                }
                printFiled(false);
                if (checkWin()) {
                    break;
                }
                gameStarted = false;
            }
        }
    }

    private void openNearFreeCells(int y, int x) {
        Deque<Ceil> stack = new ArrayDeque<>();
        map[y][x].setChecked(true);
        map[y][x].setAllHintsTrue();
        stack.offerLast(map[y][x]);
        while (!stack.isEmpty()) {
            Ceil ceil = stack.pollLast();
            if (ceil != null && !ceil.isWasInStack()) {
                for (Ceil nearCell : ceil.getNearCells()) {
                    if (nearCell.getValue() == '/') {
                        nearCell.setChecked(true);
                        nearCell.setAllHintsTrue();
                        if (!stack.contains(nearCell)) {
                            stack.offerLast(nearCell);
                        }
                    }
                }
                ceil.setWasInStack(true);
            }
        }
    }

    private boolean checkWin() {
        int match = 0;
        int notMatch = 0;
        int checked = 0;
        for (int y = 1; y < size - 1; y++) {
            for (int x = 1; x < size - 1; x++) {
                if (map[y][x].getValue() == 'X' && map[y][x].isMarked()) {
                    match++;
                }
                if (map[y][x].getValue() == '/' && map[y][x].isMarked()) {
                    notMatch++;
                }
                if (map[y][x].isChecked()) {
                    checked++;
                }
            }
        }
        if ((match == minesCount && notMatch == 0) || (9 * 9 - minesCount == checked)) {
            System.out.println("Congratulations! You found all the mines!");
            return true;
        }
        return false;
    }

    private int initCoordinate(String s) {
        int i;
        try {
            i = Integer.parseInt(s);
        } catch (NumberFormatException e) {
            System.out.println("Error! Put number! Try again.");
            return -1;
        }
        if (i < 1 || i > 9) {
            System.out.println("Error! Wrong coordinate! Try again.");
            return -1;
        }
        return i;
    }

    private void initMap() {
        map = new Ceil[size][size];
        for (int y = 0; y < size; y++) {
            for (int x = 0; x < size; x++) {
                if (y == 0 || y == 10 || x == 0 || x == 10) {
                    map[y][x] = new Ceil('B');
                } else {
                    map[y][x] = new Ceil('/');
                }
            }
        }
        putMines();
        putHints();
        putNearCells();
    }

    private void putNearCells() {
        for (int y = 1; y < size - 1; y++) {
            for (int x = 1; x < size - 1; x++) {
                Ceil[] cells = new Ceil[8];
                cells[0] = map[y][x - 1];
                cells[1] = map[y][x + 1];
                cells[2] = map[y - 1][x];
                cells[3] = map[y + 1][x];
                cells[4] = map[y - 1][x - 1];
                cells[5] = map[y - 1][x + 1];
                cells[6] = map[y + 1][x - 1];
                cells[7] = map[y + 1][x + 1];
                map[y][x].setNearCells(cells);
            }
        }
    }

    private void putMines() {
        Random random = new Random();
        while (countMines() != minesCount) {
            int y = random.nextInt(size - 2) + 1;
            int x = random.nextInt(size - 2) + 1;
            map[y][x].setValue('X');
        }
    }

    private int countMines() {
        int count = 0;
        for (int y = 1; y < size - 1; y++) {
            for (int x = 1; x < size - 1; x++) {
                if (map[y][x].getValue() == 'X') {
                    count++;
                }
            }
        }
        return count;
    }

    private void putHints() {
        for (int y = 1; y < size - 1; y++) {
            for (int x = 1; x < size - 1; x++) {
                if (map[y][x].getValue() != 'X') {
                    int hint = checkCeilAround(map, y, x);
                    if (hint != 0) {
                        map[y][x].setValue(Character.forDigit(hint, Character.MAX_RADIX));
                        map[y][x].setHint(true);
                    }
                }
            }
        }
    }

    public void printFiled(boolean isEnd) {
        System.out.println(" |123456789|");
        System.out.println("-|---------|");
        for (int y = 1; y < size - 1; y++) {
            System.out.print(y + "|");
            for (int x = 1; x < size - 1; x++) {
                if (map[y][x].isChecked()) {
                    System.out.print(map[y][x].getValue());
                } else {
                    if (map[y][x].isMarked()) {
                        System.out.print("*");
                    } else {
                        if (isEnd && map[y][x].getValue() == 'X') {
                            System.out.print(map[y][x].getValue());
                        } else {
                            System.out.print('.');
                        }
                    }
                }
            }
            System.out.println("|");
        }
        System.out.println("-|---------|");
    }

    private static int checkCeilAround(Ceil[][] map, int y, int x) {
        int count = 0;
        if (map[y - 1][x - 1].getValue() == 'X') {
            count++;
        }
        if (map[y - 1][x].getValue() == 'X') {
            count++;
        }
        if (map[y - 1][x + 1].getValue() == 'X') {
            count++;
        }
        if (map[y][x - 1].getValue() == 'X') {
            count++;
        }
        if (map[y][x + 1].getValue() == 'X') {
            count++;
        }
        if (map[y + 1][x - 1].getValue() == 'X') {
            count++;
        }
        if (map[y + 1][x].getValue() == 'X') {
            count++;
        }
        if (map[y + 1][x + 1].getValue() == 'X') {
            count++;
        }
        return count;
    }
}

class Ceil {

    private char value;
    private boolean isMarked = false;
    private boolean isChecked = false;
    private boolean isHint = false;
    private Ceil[] nearCells;
    private boolean wasInStack = false;

    public Ceil(char value) {
        this.value = value;
    }

    public char getValue() {
        return value;
    }

    public boolean isMarked() {
        return isMarked;
    }

    public boolean isChecked() {
        return isChecked;
    }

    public boolean isHint() {
        return isHint;
    }

    public Ceil[] getNearCells() {
        return nearCells;
    }

    public boolean isWasInStack() {
        return wasInStack;
    }

    public void setValue(char value) {
        this.value = value;
    }

    public void setMarked(boolean mark) {
        isMarked = mark;
    }

    public void setChecked(boolean checked) {
        isChecked = checked;
    }

    public void setHint(boolean hint) {
        isHint = hint;
    }

    public void setNearCells(Ceil[] nearCells) {
        this.nearCells = nearCells;
    }

    public void setWasInStack(boolean wasInStack) {
        this.wasInStack = wasInStack;
    }

    public void setAllHintsTrue() {
        for (Ceil nearCell : nearCells) {
            if (nearCell.isHint()) {
                nearCell.setChecked(true);
            }
        }
    }
}
