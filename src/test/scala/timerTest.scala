/******************************************************************
* Filename:      timerTest.scala
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

class Timer_Test(c: PWM) extends PeekPokeTester(c) {
   var timer_cfg = 7
   var timer_duty = 5
  var timer_reset = 21

   poke(c.io.reg_cfg_di, timer_cfg.U)
   poke(c.io.reg_cfg_we, true.B)
   step(1)
  poke(c.io.reg_cfg_we, false.B)
  poke(c.io.reg_duty_di, timer_duty.U)
  poke(c.io.reg_duty_we, true.B)
  step(1)
  poke(c.io.reg_duty_we, false.B)
  poke(c.io.reg_dat_di, 0.U)
  poke(c.io.reg_dat_we, true.B)
  step(1)
  poke(c.io.reg_dat_we, false.B)
  poke(c.io.reg_val_di, timer_reset.U)
  poke(c.io.reg_val_we, true.B)
  step(1)
  poke(c.io.reg_val_we, false.B)

  for (i <- 0 until 110) {
    step(1)
  }

  timer_duty = 10
  poke(c.io.reg_duty_di, timer_duty.U)
  poke(c.io.reg_duty_we, true.B)
  step(1)
  poke(c.io.reg_duty_we, false.B)

  for (i <- 0 until 110) {
    step(1)
  }

  timer_duty = 15
  poke(c.io.reg_duty_di, timer_duty.U)
  poke(c.io.reg_duty_we, true.B)
  step(1)
  poke(c.io.reg_duty_we, false.B)

  for (i <- 0 until 110) {
    step(1)
  }

}

// object for tester class 
object Timer_Tester_Main extends App {

  iotesters.Driver.execute(Array("--generate-vcd-output", "on", "--backend-name", "treadle"),
    () => new PWM) { c => new Timer_Test(c)
  }
}
