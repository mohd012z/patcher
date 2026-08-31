package com.msa.patcher.report
import org.junit.Assert.*
import org.junit.Test
class ConfidenceCalculatorTest {
 @Test fun scoresAreIndependentAndBounded() {
  val a=ConfidenceCalculator.calculate(10,10,0,0,0,10)
  assertEquals(100,a.analysisCoverage); assertEquals(20,a.behaviourConfidence)
  val b=ConfidenceCalculator.calculate(1,10,10,0,0,0)
  assertEquals(10,b.analysisCoverage); assertEquals(100,b.behaviourConfidence)
 }
}
