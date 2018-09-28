/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package search;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.Files;
import java.nio.file.SimpleFileVisitor;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreeNode;

/**
 * The instance of this class is looking for files and construct the tree from 
 * found files, root of which you can get using the getRoot.
 * 
 * @author evgenii
 */
public class FileSearcher extends SimpleFileVisitor<Path> {

    private static final Logger LOGGER
            = Logger.getLogger(FileSearcher.class.getName());
    private final Stack<List> levels = new Stack();
    private List<MutableTreeNode> lastLevel = new ArrayList<>();
    private final String ext;
    private final String pat;

    /**
     * Initializes a new instance of this class
     * 
     * @param ext extension of required files
     * @param pat substring that FileSearcher searches in files
     */
    public FileSearcher(String ext, String pat) {
        this.ext = ext.toLowerCase().trim();
        this.pat = pat.toLowerCase();
    }

    @Override
    public FileVisitResult preVisitDirectory(Path dir,
            BasicFileAttributes attrs) throws IOException {

        levels.push(lastLevel);
        lastLevel = new ArrayList<>();

        LOGGER.log(Level.INFO, "Enter to dir: {0}", dir.toString());

        return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs)
            throws IOException {

        if (Files.isReadable(file) && file.toString().endsWith(ext)) {

            try (Stream<String> fileStream = Files.lines(file)) {

                if (fileStream.map(String::toLowerCase)
                        .anyMatch(s -> s.contains(pat))) {

                    String fileName = file.getFileName().toString();
                    MutableTreeNode newNode
                            = new DefaultMutableTreeNode(fileName);
                    lastLevel.add(newNode);

                    LOGGER.log(Level.INFO, "found file: {0}", fileName);
                }
            } catch (IOException ex) {
                LOGGER.log(Level.INFO, "IOException in {0}", file.toString());
            } finally {
                LOGGER.log(Level.INFO, "visit file:{0}", file.toString());
                return FileVisitResult.CONTINUE;
            }
        }
        return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult visitFileFailed(Path file, IOException exc)
            throws IOException {

        return FileVisitResult.SKIP_SUBTREE;
    }

    @Override
    public FileVisitResult postVisitDirectory(Path dir, IOException exc)
            throws IOException {

        if (!lastLevel.isEmpty()) {
            String dirName = dir.getFileName().toString();
            DefaultMutableTreeNode newNode
                    = new DefaultMutableTreeNode(dirName);
            lastLevel.forEach(node -> newNode.add(node));
            newNode.setUserObject(dirName);
            lastLevel = levels.pop();
            lastLevel.add(newNode);
        } else {
            lastLevel = levels.pop();
        }

        LOGGER.log(Level.INFO, "Exit from dir: {0}", dir.toString());
        return FileVisitResult.CONTINUE;
    }

    /** 
     * 
     * @return root of files found tree or null if files is not found
     */
    public TreeNode getRoot() {
        return lastLevel.isEmpty() ? null : lastLevel.get(0);
    }

}
