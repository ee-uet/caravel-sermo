package MC_Module

import chisel3._
import chisel3.util.Cat

class MotorIO extends Bundle {
  // Wishbone bus signals
  val wb_adr_i =  Input(UInt(32.W))
  val wb_dat_i =  Input(UInt(32.W))
  val wb_sel_i =  Input(UInt(4.W))
  val wb_we_i =  Input(Bool())
  val wb_cyc_i =  Input(Bool())
  val wb_stb_i =  Input(Bool())
  val wb_ack_o =  Output(Bool())
  val wb_dat_o =  Output(UInt(32.W))

  val irq =  Output(Bool())

  // QEI IOs
  val QEI_ChA = Input(Bool())
  val QEI_ChB = Input(Bool())
 // val Speed =  Output(UInt(32.W))

  // PWM IOs
  val pwm_h =  Output(Bool())
  val pwm_l =  Output(Bool())
}


class Motor_Top extends Module{

  val io = IO(new MotorIO)

  val bus_dat_i = io.wb_dat_i

  // PID output
  val pid_out = Wire(SInt(16.W))

  // Interlink module IO connections
  val IL_module = Module(new Interlink_Module)

  // Wiring WB bus with interlink module
  IL_module.io.bus_stb_i := io.wb_stb_i
  IL_module.io.bus_cyc_i := io.wb_cyc_i
  IL_module.io.bus_adr_i := io.wb_adr_i
  IL_module.io.bus_sel_i := io.wb_sel_i
  IL_module.io.bus_we_i := io.wb_we_i
  io.wb_dat_o := IL_module.io.bus_dat_o
  io.wb_ack_o := IL_module.io.bus_ack_o

  // Timer module IO connections
  val timer_module = Module(new PWM)
   timer_module.io.reg_val_we := IL_module.io.tmr_val_we
   timer_module.io.reg_val_di := bus_dat_i
   IL_module.io.tmr_val_do :=  timer_module.io.reg_val_do

   timer_module.io.reg_cfg_we := IL_module.io.tmr_cfg_we
   timer_module.io.reg_cfg_di := bus_dat_i
   IL_module.io.tmr_cfg_do :=  timer_module.io.reg_cfg_do

   timer_module.io.reg_dat_we := IL_module.io.tmr_dat_we
   timer_module.io.reg_dat_di := bus_dat_i
   IL_module.io.tmr_dat_do :=  timer_module.io.reg_dat_do

   timer_module.io.reg_duty_we := IL_module.io.tmr_duty_we
   timer_module.io.reg_duty_di := bus_dat_i
   IL_module.io.tmr_duty_do :=  timer_module.io.reg_duty_do

   timer_module.io.reg_pid_out := pid_out
   io.irq :=  timer_module.io.irq_out

  // pwm IO connections
   io.pwm_h :=  timer_module.io.pwm_h
   io.pwm_l :=  timer_module.io.pwm_l


  // QEI module and IO connections
  val QEI = Module(new Quad_Encoder)
  QEI.io.quadA := io.QEI_ChA
  QEI.io.quadB := io.QEI_ChB

  QEI.io.reg_count_we := IL_module.io.qei_count_we
  QEI.io.reg_count_di := bus_dat_i
  IL_module.io.qei_count_do := QEI.io.reg_count_do

  QEI.io.reg_cfg_we := IL_module.io.qei_cfg_we
  QEI.io.reg_cfg_di := bus_dat_i
  IL_module.io.qei_cfg_do := QEI.io.reg_cfg_do

  IL_module.io.qei_speed_do := QEI.io.reg_speed_do

  // PID module and IO connections
  val PID = Module(new PID_Controller)
  PID.io.rst := false.B
  PID.io.speed_fb_in := QEI.io.reg_speed_do

  PID.io.reg_kp_we := IL_module.io.pid_kp_we
  PID.io.reg_kp_di := bus_dat_i(7,0).asSInt()
  IL_module.io.pid_kp_do  := PID.io.reg_kp_do

  PID.io.reg_ki_we := IL_module.io.pid_ki_we
  PID.io.reg_ki_di := bus_dat_i(7,0).asSInt()
  IL_module.io.pid_ki_do  := PID.io.reg_ki_do

  PID.io.reg_kd_we := IL_module.io.pid_kd_we
  PID.io.reg_kd_di := bus_dat_i(7,0).asSInt()
  IL_module.io.pid_kd_do  := PID.io.reg_kd_do

  PID.io.reg_ref_we := IL_module.io.pid_ref_we
  PID.io.reg_ref_di := bus_dat_i(15,0).asSInt()
  IL_module.io.pid_ref_do  := PID.io.reg_ref_do

  PID.io.reg_fb_we := IL_module.io.pid_fb_we
  PID.io.reg_fb_di := bus_dat_i(15,0).asSInt()
  IL_module.io.pid_fb_do  := PID.io.reg_fb_do

  PID.io.reg_cfg_we := IL_module.io.pid_cfg_we
  PID.io.reg_cfg_di := bus_dat_i(15,0).asSInt()
  IL_module.io.pid_cfg_do  := PID.io.reg_cfg_do

  pid_out := PID.io.pid_out
  PID.io.raw_irq := timer_module.io.rawirq_out

}

object Motor_generate extends App {
  //(new chisel3.stage.ChiselStage).execute(args, () => new quad)

  chisel3.Driver.execute(args, () => new Motor_Top)
}