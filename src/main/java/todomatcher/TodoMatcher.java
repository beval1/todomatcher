package todomatcher;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class TodoMatcher {
    private boolean inString = false;
    private boolean inLineComment = false;
    private int inLineCommentStartIndex = -1;
    private int inLineCommentEndIndex = -1;
    private boolean inMultiComment = false;
    private int inMultiLineStartIndex = -1;
    private int inMultiLineEndIndex = -1;
    List<String> matches = new ArrayList<>();


    private void listFilesRecursively(File root) throws FileNotFoundException {
        File[] listOfFilesAndDirectory = root.listFiles();

        if (listOfFilesAndDirectory != null) {
            for (File file : listOfFilesAndDirectory) {
                inString = false;
                inLineComment = false;
                inLineCommentStartIndex = -1;
                inLineCommentEndIndex = -1;
                inMultiComment = false;
                inMultiLineEndIndex = -1;
                inMultiLineStartIndex = -1;

                if (file.isDirectory()) {
                    listFilesRecursively(file);
                } else {
                    if (file.getName().endsWith(".java")) {
                        matchFile(file);
                    }
                }
            }
        }
    }

    public void matchDir() throws FileNotFoundException {
        String userDir = System.getProperty("user.dir");
        listFilesRecursively(new File(userDir));
    }

    public void matchFile(File file) throws FileNotFoundException {
        Scanner in = new Scanner(new FileReader(file.getPath()));
        StringBuilder sb = new StringBuilder();

        while (in.hasNextLine()) {
            String line = in.nextLine();
            sb.append(line);
            sb.append("\n");
        }
        in.close();

        for (int i = 0; i < sb.length(); i++) {
            isInString(sb, i);
            String lastTwoSymbols = i >= 2 ? sb.substring(i-2, i) : "";
            isCharInLineComment(lastTwoSymbols, sb, i);
            isCharInMultiComment(lastTwoSymbols, sb, i);
        }

        matches.forEach(System.out::println);
    }

    private void isCharInLineComment(String lastTwoSymbols, StringBuilder sb, int i){
        if (lastTwoSymbols.equals("//") && !inString && !inLineComment){
            //get till end of line
            inLineComment = true;
            inLineCommentStartIndex = i-2;
        } else if (inLineComment && sb.charAt(i) == '\n'){
            inLineComment = false;
            inLineCommentEndIndex = i;
            String commentLine = sb.substring(inLineCommentStartIndex, inLineCommentEndIndex);
            if (commentLine.contains("TODO")) {
                matches.add(commentLine);
            }
        }
    }

    private void isCharInMultiComment(String lastTwoSymbols, StringBuilder sb, int i){
        if (lastTwoSymbols.equals("/*") && !inString){
            inMultiComment = true;
            inMultiLineStartIndex = i-2;
        } else if (inMultiComment && lastTwoSymbols.equals("*/")){
            inMultiComment = false;
            inMultiLineEndIndex = i;
            String commentLine = sb.substring(inMultiLineStartIndex, inMultiLineEndIndex);
            if (commentLine.contains("TODO")) {
                matches.add(commentLine);
            }
        }
    }

    private void isInString(StringBuilder sb, int i) {
        if (sb.charAt(i) == '"' && !inString && !inLineComment && !inMultiComment) {
            //invalidate if in character literal
            if (sb.charAt(i-1) != '\'') {
                inString = true;
            }
        } else if (sb.charAt(i) == '"' && inString) {
            inString = false;
        }
    }
}
