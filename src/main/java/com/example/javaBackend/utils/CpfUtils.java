package com.example.javaBackend.utils;

public class CpfUtils {
    public static final String CPF_MASK = "###.###.###-##";

    public static boolean verifyIfValid(String cpfNumber) throws Exception {

        int[] expectedValidationNumbers = CalcValidationNumbers(cpfNumber);

        if (expectedValidationNumbers[0] == Character.getNumericValue(cpfNumber.charAt(9))
                && expectedValidationNumbers[1] == Character.getNumericValue(cpfNumber.charAt(10))) {
            return true;
        } else return false;

    }

    private static int[] CalcValidationNumbers(String cpfNumber) throws Exception {

        if(cpfNumber.length() != 9 && cpfNumber.length() != 11){
            throw new Exception("quantidade de caracteres deve ser igual a 9 ou 11 para o cálculo");
        }

        int vd1NumbersQtd = 10;
        int vd2NumbersQtd = 11;
        int vd1Sum = 0;
        int vd2Sum = 0;
        for (int i = 0; i < 9; i++) {
            vd1Sum += Character.getNumericValue(cpfNumber.charAt(i)) * vd1NumbersQtd;
            vd2Sum += Character.getNumericValue(cpfNumber.charAt(i)) * vd2NumbersQtd;
            vd1NumbersQtd--;
            vd2NumbersQtd--;
        }


        int expectedVd1 = calculateVd(vd1Sum);
        vd2Sum += expectedVd1 * 2;
        int expectedVd2 = calculateVd(vd2Sum);

        return new int[]{expectedVd1, expectedVd2};
    }

    private static int calculateVd(int vdSum) {
        int remainder = vdSum % 11;
        int verifyingDigit = 0;
        if (remainder > 1) {
            verifyingDigit = 11 - remainder;
        }
        return verifyingDigit;
    }

}
