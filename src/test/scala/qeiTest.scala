/******************************************************************
* Filename:      qeiTest.scala
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

class QEI_Test(c: Quad_Encoder) extends PeekPokeTester(c) {
   var data_in = 6
   var count = BigInt(0)

   poke(c.io.quadA, false.B)
   poke(c.io.quadB, false.B)
   poke(c.io.reg_count_we, false.B)
   poke(c.io.reg_count_di, data_in.U)
   step(1)

  for (i <- 0 until 10) {
    poke(c.io.quadA, true.B)
    poke(c.io.quadB, false.B)
    step(1)
    poke(c.io.quadA, true.B)
    poke(c.io.quadB, true.B)
    step(1)
    poke(c.io.quadA, false.B)
    poke(c.io.quadB, true.B)
    step(1)
    poke(c.io.quadA, false.B)
    poke(c.io.quadB, false.B)
    step(1)
    count = peek(c.io.reg_count_do)
   // count = count + 1
  }

  for (i <- 0 until 4) {
    poke(c.io.quadB, true.B)
    poke(c.io.quadA, false.B)
    step(1)
    poke(c.io.quadA, true.B)
    poke(c.io.quadB, true.B)
    step(1)
    poke(c.io.quadB, false.B)
    poke(c.io.quadA, true.B)
    step(1)
    poke(c.io.quadA, false.B)
    poke(c.io.quadB, false.B)
    step(1)
    count = peek(c.io.reg_count_do)
    // count = count + 1
  }


  if(count == 10)
    count = 0

}

// object for tester class 
object QEI_Tester_Main extends App {

  iotesters.Driver.execute(Array("--generate-vcd-output", "on", "--backend-name", "treadle"), () => new Quad_Encoder){
    c => new QEI_Test(c)
  }
}
