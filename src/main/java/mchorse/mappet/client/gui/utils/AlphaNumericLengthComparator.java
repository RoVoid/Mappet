package mchorse.mappet.client.gui.utils;

import java.util.Comparator;

public class AlphaNumericLengthComparator implements Comparator<String> {
    @Override
    public int compare(String a, String b) {
        int i = 0, j = 0;
        int n1 = a.length(), n2 = b.length();

        while (i < n1 && j < n2) {
            char c1 = a.charAt(i);
            char c2 = b.charAt(j);

            boolean c1Digit = Character.isDigit(c1);
            boolean c2Digit = Character.isDigit(c2);

            if (c1Digit && c2Digit) {
                int startI = i;
                while (i < n1 && Character.isDigit(a.charAt(i))) i++;
                int startJ = j;
                while (j < n2 && Character.isDigit(b.charAt(j))) j++;

                String num1 = a.substring(startI, i);
                String num2 = b.substring(startJ, j);

                int cmp = Integer.compare(Integer.parseInt(num1), Integer.parseInt(num2));
                if (cmp != 0) return cmp;
            } else if (c1Digit) {
                return 1;
            } else if (c2Digit) {
                return -1;
            } else {
                int cmp = Character.compare(Character.toLowerCase(c1), Character.toLowerCase(c2));
                if (cmp != 0) return cmp;
                i++; j++;
            }
        }

        return Integer.compare(n1, n2);
    }
}

