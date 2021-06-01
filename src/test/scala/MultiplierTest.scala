/******************************************************************
* Filename:      MultiplierTest.scala
* Date:          07-05-2021
* Author:        M Tahir
*
* Description:   A simple tester based on chisel3 iotesters  
*                using FIRRTL compiler.
*
* Issues:        
*                 
******************************************************************/

package MC_Module
import chisel3._
import chisel3.iotesters.{Driver, PeekPokeTester}
import util.vedic_8x8

class Multiplier_Test(c: vedic_8x8) extends PeekPokeTester(c) {
   var data_in1 = -13
   var data_in2 = 24

   poke(c.io.a, data_in1.S)
   poke(c.io.b, data_in2.U)
   step(1)

  var product = peek(c.io.c)
  step(1)
  data_in1 = 56
  data_in2 = 73

  poke(c.io.a, data_in1.S)
  poke(c.io.b, data_in2.U)
  step(1)

  product = peek(c.io.c)
  step(1)

  data_in1 = -75
  data_in2 = 82

  poke(c.io.a, data_in1.S)
  poke(c.io.b, data_in2.U)
  step(1)

  product = peek(c.io.c)
  step(1)

}

// object for tester class 
object Multiplier_Tester_Main extends App {

  iotesters.Driver.execute(Array("--generate-vcd-output", "on", "--backend-name", "treadle"), () => new vedic_8x8){
    c => new Multiplier_Test(c)
  }
}
