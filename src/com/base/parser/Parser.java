package com.base.parser;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.*;

/**
 * Created by Arshad on 2/25/2017.
 */
public class Parser {
    public static void main(String args[]) {
        readInput();
    }

    private static void readInput() {
        StringBuilder sbText = new StringBuilder();
        StringBuilder sbDoc = new StringBuilder();
        File dir=new File("files");
        File[] listOfFiles=dir.listFiles();
        for (File f:listOfFiles) {
            try (FileReader readFile = new FileReader(f);
                 BufferedReader bRead = new BufferedReader(readFile);) {
                while (true) {
                    String line = bRead.readLine();
                    if (line == null) {
                        break;
                    } else if (line.toLowerCase().contains(("<DOCNO>").toLowerCase())) {
                        String noSt = line.substring(line.indexOf("FT"), line.indexOf("</"));
                        sbDoc = sbDoc.append(noSt).append(System.getProperty("line.separator"));
                    } else if (line.toLowerCase().contains(("<TEXT>").toLowerCase())) {
                        line = bRead.readLine();
                        while (!line.equalsIgnoreCase("</TEXT>")) {
                            sbText = sbText.append(line).append(' ');
                            line = bRead.readLine();
                        }
                    } else {
                        continue;
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        createTokens(sbText);
        createDocList(sbDoc);
    }

    private static void createTokens(StringBuilder sbText) {
        String tokens[] = sbText.toString().toLowerCase().split("\\W+"/*"\\s"*/);
        List<String> stopWords = getStopWords();
        Map<Integer, String> wordDictionary = getParsedTokens(tokens, stopWords);
        printDictionary(wordDictionary, false);
    }

    private static Map<Integer, String> getParsedTokens(String[] tokens, List<String> stopWords) {
        Set<String> parsedTokens = new LinkedHashSet<>();
        //System.out.println("Initial length of tokens after split with spaces is: " + tokens.length);
        Porter myporter = new Porter();
        for (String t : tokens) {
            if (t.isEmpty() || t.matches(".*\\d.*")) {
                continue;
            } else {
                t = t.replaceAll("[^a-zA-Z ]", "");
                if (!t.isEmpty() && !stopWords.contains(t)) {
                    t = myporter.stripAffixes(t);
                    if (!t.isEmpty())
                        parsedTokens.add(t);
                }
            }
        }
        //parsedTokens.removeAll(stopWords);
        //System.out.println("Total length after removing empty words,numbers and porter algo " +
        //        "is: " + parsedTokens.toArray().length);
        TreeSet<String> finalTokens = new TreeSet<String>();
        finalTokens.addAll(parsedTokens);
        Map<Integer, String> wordDictionary = new LinkedHashMap<>();
        int key = 0;
        Iterator<String> itr = finalTokens.iterator();
        while (itr.hasNext()) {
            wordDictionary.put(++key, itr.next());
        }
        return wordDictionary;
    }

    private static List<String> getStopWords() {
        List<String> stopWords = new ArrayList<>();
        try (FileReader readStopWords = new FileReader("files/stopwordlist.txt");
             BufferedReader brST = new BufferedReader(readStopWords)) {
            while (true) {
                String line = brST.readLine();
                if (line == null)
                    break;
                else
                    stopWords.add(line.trim());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return stopWords;
    }

    private static void createDocList(StringBuilder sbDoc) {
        String docNames[] = sbDoc.toString().split("\n");
        Map<Integer, String> fileDictionary = new LinkedHashMap<>();
        for (int i = 1; i <= docNames.length; i++) {
            fileDictionary.put(i, docNames[i - 1].trim());
        }
        printDictionary(fileDictionary, true);
    }

    private static void printDictionary(Map<Integer, String> map, Boolean append) {
        try (FileWriter writeTokens = new FileWriter("Output/Parser_Output.txt", append)) {
            for (int i=1;i<=map.size();i++) {
                //String value = map.get(k).toString();
                //System.out.println(map.get(i)+" "+i);
                writeTokens.write(map.get(i)+" "+i+"\n");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
