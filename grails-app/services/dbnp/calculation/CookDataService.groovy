/**
 * Cookdata Service
 * Provides business logic for Cookdata controller
 */
package dbnp.calculation

import java.util.List;
import java.util.Map;
import java.lang.Math
import grails.converters.JSON

class CookdataService {

    private boolean checkForOpeningAndClosingBrackets(String eq){
        boolean ret = (
        // ( ... ) and
        eq.startsWith("(") && eq.endsWith(")")
                &&
                // First ) is at the last index
                eq.indexOf(")")==(eq.size()-1)
        )
        return ret
    }

    private Map parseWellFormedLeftHandSide(String eq){
        Map mapReturn = [:]
        int countOpening = 0
        int countClosing = 0
        int latestClosingIndex = -1
        for(int i = 0; i < eq.size(); i++){
            if(eq[i]=="("){
                countOpening++
            }
            if(eq[i]==")"){
                countClosing++
                latestClosingIndex = i
            }
            if(	countOpening!=0 && countClosing!=0 &&
                    countOpening==countClosing
            ){
                // Left-hand side is wellformed
                break
            }
        }

        mapReturn.endIndex = 1 + latestClosingIndex
        if(mapReturn.endIndex==eq.size()){
            // Brackets are part of right-hand side, not left...
            mapReturn.endIndex = 0
        }
        mapReturn.success = (countOpening==countClosing)
        return mapReturn
    }

    private double computeWithVals(String eq, int counter, double dblA, double dblB){
        double dblReturn = -1.0
        // Check for "(x)"
        if(checkForOpeningAndClosingBrackets(eq)){
            int index0 = eq.indexOf(")")
            double result = computeWithVals(eq.substring(1, index0), counter+1, dblA, dblB)
            dblReturn = result
            return dblReturn
        }

        /* Check for "x/y" and make sure "(x/y)/z" detects the second operator,
           * not the last.
           */
        Map mapParseLHSResults = parseWellFormedLeftHandSide(eq)
        if(mapParseLHSResults.success){
            // A wellformed LHS could be found. Any operator after the LHS?
            int intOpIndex = eq.substring(mapParseLHSResults.endIndex,
                    eq.size()).indexOf("/")
            if(intOpIndex!=-1){
                int index0 = intOpIndex+mapParseLHSResults.endIndex
                double result1 = computeWithVals(eq.substring(0, index0), counter+1, dblA, dblB)
                double result2 = computeWithVals(eq.substring(index0+1, eq.size()), counter+2, dblA, dblB)
                dblReturn = result1 / result2
                return dblReturn
            }
            intOpIndex = eq.substring(mapParseLHSResults.endIndex,
                    eq.size()).indexOf("*")
            if(intOpIndex!=-1){
                int index0 = intOpIndex+mapParseLHSResults.endIndex
                double result1 = computeWithVals(eq.substring(0, index0), counter+1, dblA, dblB)
                double result2 = computeWithVals(eq.substring(index0+1, eq.size()), counter+2, dblA, dblB)
                dblReturn = result1 * result2
                return dblReturn
            }
            intOpIndex = eq.substring(mapParseLHSResults.endIndex,
                    eq.size()).indexOf("+")
            if(intOpIndex!=-1){
                int index0 = intOpIndex+mapParseLHSResults.endIndex
                double result1 = computeWithVals(eq.substring(0, index0), counter+1, dblA, dblB)
                double result2 = computeWithVals(eq.substring(index0+1, eq.size()), counter+2, dblA, dblB)
                dblReturn = result1 + result2
                return dblReturn
            }
            intOpIndex = eq.substring(mapParseLHSResults.endIndex,
                    eq.size()).indexOf("-")
            if(intOpIndex!=-1){
                int index0 = intOpIndex+mapParseLHSResults.endIndex
                double result1 = computeWithVals(eq.substring(0, index0), counter+1, dblA, dblB)
                double result2 = computeWithVals(eq.substring(index0+1, eq.size()), counter+2, dblA, dblB)
                dblReturn = result1 - result2
                return dblReturn
            }
        } else {
            throw new Exception( "computeWithVals encountered a malformed equation: " + eq )
            println "computeWithVals encountered a malformed equation: "+eq
        }

        // Check for A
        if(eq.equals("A")){
            dblReturn = dblA
            return dblReturn
        }

        // Check for B
        if(eq.equals("B")){
            dblReturn = dblB
            return dblReturn
        }

        throw new Exception( "computeWithVals encountered an equation it failed to parse: " + eq )
        println "computeWithVals encountered an equation it failed to parse: "+eq
    }

    /**
     * Computes the mean of the given values. Values that can not be parsed to a number
     * are ignored.
     * @param values List of values to compute the mean for
     * @return Arithmetic mean of the values
     */
    private double computeMean(List values){
        def sumOfValues = 0
        def sizeOfValues = 0
        values.each { value ->
            if(value!=null){
                sumOfValues += value
                sizeOfValues++
            }
        }
        return sumOfValues / sizeOfValues
    }

    /**
     * Computes the median of the given values. Values that can not be parsed to a number
     * are ignored.
     * @param values List of values to compute the mean for
     * @return Arithmetic mean of the values
     */
    private double computeMedian(List values){
        List newValues = []
        values.each { value ->
            if(value!=null){
                newValues.add(value)
            }
        }
        newValues.sort()
        int listSize = newValues.size() - 1
        def intPointer = (int) Math.abs(listSize * 0.5)
        if(intPointer == (listSize * 0.5)){
            // If we exactly end up at an item, take this item
            return newValues.get(intPointer);
        } else {
            // If we don't exactly end up at an item, take the mean of the 2 adjacent values
            return ((newValues.get(intPointer) + newValues.get(intPointer + 1)) / 2);
        }
    }
}
