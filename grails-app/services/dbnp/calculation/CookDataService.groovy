/**
 * ParserService Service
 * 
 * Description of my service
 *
 * @author  your email (+name?)
 * @since	2010mmdd
 * @package	???
 *
 * Revision information:
 * $Rev: 1430 $
 * $Author: work@osx.eu $
 * $Date: 2011-01-21 21:05:36 +0100 (Fri, 21 Jan 2011) $
 */
package dbnp.calculation

import java.util.Map;
import grails.converters.JSON

class CookDataService {

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
			println "Received malformed string: unbalanced brackets: "+eq
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
		
		// If we get here, nothing has fired
		dblReturn = -1.0
		return dblReturn
	}
	
}
