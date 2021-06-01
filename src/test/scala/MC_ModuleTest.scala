/******************************************************************
* Filename:      MC_ModuleTest.scala
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
import core.Motor_Top

class MC_Module_Test(c: Motor_Top) extends PeekPokeTester(c) {
  var timer_cfg = 0x2F
  var speed_count = 0xC030 // speed enable and count value for QEI speed clculation
  var timer_reset = 0x100
  var pid_ref = 0x17
  var pid_kp = 0x1
  var pid_ki = 0x1

  var tmr_cfg_addr = 0x30000000
  var tmr_val_addr = 0x30000004
  var qei_cfg_addr = 0x30000108
  var qei_count_addr = 0x30000100
  var pid_ref_addr = 0x3000020C
  var pid_kp_addr = 0x30000200
  var pid_ki_addr = 0x30000204


  poke(c.io.QEI_ChA, false.B)
  poke(c.io.QEI_ChB, false.B)

  poke(c.io.wb_adr_i, tmr_val_addr)
  poke(c.io.wb_dat_i, timer_reset)
  poke(c.io.wb_sel_i, 3.U)
  poke(c.io.wb_we_i, true.B)
  poke(c.io.wb_cyc_i, true.B)
  poke(c.io.wb_stb_i, true.B)
  step(1)
  poke(c.io.wb_cyc_i, false.B)
  poke(c.io.wb_stb_i, false.B)
  step(1)

  poke(c.io.wb_adr_i, tmr_cfg_addr)
  poke(c.io.wb_dat_i, timer_cfg)
  poke(c.io.wb_cyc_i, true.B)
  poke(c.io.wb_stb_i, true.B)
  step(1)
  poke(c.io.wb_cyc_i, false.B)
  poke(c.io.wb_stb_i, false.B)
  step(1)

  poke(c.io.wb_adr_i, qei_cfg_addr)
  poke(c.io.wb_dat_i, speed_count)
  poke(c.io.wb_cyc_i, true.B)
  poke(c.io.wb_stb_i, true.B)
  step(1)
  poke(c.io.wb_cyc_i, false.B)
  poke(c.io.wb_stb_i, false.B)
  step(1)

  poke(c.io.wb_adr_i, pid_ref_addr)
  poke(c.io.wb_dat_i, pid_ref)
  poke(c.io.wb_cyc_i, true.B)
  poke(c.io.wb_stb_i, true.B)
  step(1)
  poke(c.io.wb_cyc_i, false.B)
  poke(c.io.wb_stb_i, false.B)
  step(1)

  poke(c.io.wb_adr_i, pid_kp_addr)
  poke(c.io.wb_dat_i, pid_kp)
  poke(c.io.wb_cyc_i, true.B)
  poke(c.io.wb_stb_i, true.B)
  step(1)
  poke(c.io.wb_cyc_i, false.B)
  poke(c.io.wb_stb_i, false.B)
  step(1)

  poke(c.io.wb_adr_i, pid_ki_addr)
  poke(c.io.wb_dat_i, pid_ki)
  poke(c.io.wb_cyc_i, true.B)
  poke(c.io.wb_stb_i, true.B)
  step(1)
  poke(c.io.wb_cyc_i, false.B)
  poke(c.io.wb_stb_i, false.B)
  step(1)


  for (i <- 0 until 200) {
    poke(c.io.QEI_ChA, true.B)
    poke(c.io.QEI_ChB, false.B)
    step(3)
    poke(c.io.QEI_ChA, true.B)
    poke(c.io.QEI_ChB, true.B)
    step(3)
    poke(c.io.QEI_ChA, false.B)
    poke(c.io.QEI_ChB, true.B)
    step(3)
    poke(c.io.QEI_ChA, false.B)
    poke(c.io.QEI_ChB, false.B)
    step(3)
  }

  poke(c.io.wb_adr_i, qei_count_addr)
 // poke(c.io.wb_dat_i, speed_count)
  poke(c.io.wb_cyc_i, true.B)
  poke(c.io.wb_stb_i, true.B)
  poke(c.io.wb_we_i, false.B)
  step(1)
  var count = peek(c.io.wb_dat_o)
  poke(c.io.wb_cyc_i, false.B)
  poke(c.io.wb_stb_i, false.B)
  step(1)


  for (i <- 0 until 200) {
    poke(c.io.QEI_ChA, true.B)
    poke(c.io.QEI_ChB, false.B)
    step(1)
    poke(c.io.QEI_ChA, true.B)
    poke(c.io.QEI_ChB, true.B)
    step(1)
    poke(c.io.QEI_ChA, false.B)
    poke(c.io.QEI_ChB, true.B)
    step(1)
    poke(c.io.QEI_ChA, false.B)
    poke(c.io.QEI_ChB, false.B)
    step(1)
  }

}

// object for tester class 
object MC_Module_Tester_Main extends App {

  iotesters.Driver.execute(Array("--generate-vcd-output", "on", "--backend-name", "treadle"),
    () => new Motor_Top) { c => new MC_Module_Test(c)
  }
}
