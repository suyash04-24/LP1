package LRU;


import java.util.ArrayList;
import java.util.HashMap;

public class LRUPageReplacement {
    static int pageFrames = 3;

    // Least Recently Used (LRU) Page Replacement Algorithm
    static int lru(int referenceString[]) {
        ArrayList<Integer> pages = new ArrayList<>(pageFrames);
        HashMap<Integer, Integer> indexes = new HashMap<>();
        int page_faults = 0, n = referenceString.length, curPage;

        for (int i = 0; i < n; i++) {
            curPage = referenceString[i];

            if (pages.size() < pageFrames) {
                if (!pages.contains(curPage)) {
                    pages.add(curPage);
                    page_faults++;
                    displayPageFrames(pages, page_faults);
                }
                indexes.put(curPage, i);
            } else {
                if (!pages.contains(curPage)) {
                    int lru = Integer.MAX_VALUE, pageToBeReplaced = 0;
                    int temp;
                    for (int j = 0; j < pages.size(); j++) {
                        temp = pages.get(j);
                        if (indexes.get(temp) < lru) {
                            lru = indexes.get(temp);
                            pageToBeReplaced = j;
                        }
                    }
                    indexes.remove(pages.get(pageToBeReplaced));
                    pages.set(pageToBeReplaced, curPage);
                    page_faults++;
                    displayPageFrames(pages, page_faults);
                }
                indexes.put(curPage, i);
            }
        }
        return page_faults;
    }

    static void displayPageFrames(ArrayList<Integer> pages, int page_faults) {
        System.out.print("At PageFault- " + page_faults + " :: Pages- ");
        for (int page : pages) {
            System.out.print(" " + page);
        }
        System.out.print("\n");
    }

    public static void main(String[] args) {
        int pages[] = {7, 0, 1, 2, 0, 3, 0, 4, 2, 3, 0, 3, 2, 1, 2, 0, 1, 7, 0, 1};
        System.out.println("--- Implementing Least Recently Used Page Replacement Algorithm ---");
        int pageFaults = lru(pages);
        System.out.println("Number of page faults = " + pageFaults);
    }
}

