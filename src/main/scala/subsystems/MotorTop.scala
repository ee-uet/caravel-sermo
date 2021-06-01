package subsystems

import chisel3._
import chisel3.util.{Cat, Fill}
import devices.{PIDController, PWM, QuadEncoder}
import shells.{CaravelShell, HasPowerPins}

class MotorTop extends CaravelShell{

  override def desiredName: String = "user_project_wrapper"

  val bus_dat_i = wbs.dat_i

  // PID output
  val pid_out = Wire(SInt(16.W))

  // Interlink module IO connections
  val IL_module = caravalModule(new InterlinkModule with HasPowerPins)

  // Wiring WB bus with interlink module
  IL_module.io.bus_stb_i := wbs.stb_i
  IL_module.io.bus_cyc_i := wbs.cyc_i
  IL_module.io.bus_adr_i := wbs.adr_i
  IL_module.io.bus_sel_i := wbs.sel_i
  IL_module.io.bus_we_i := wbs.we_i
  wbs.dat_o := IL_module.io.bus_dat_o
  wbs.ack_o := IL_module.io.bus_ack_o

  // Timer module IO connections
  val timer_module = caravalModule(new PWM with HasPowerPins)
  timer_module.wb <> wb
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
   irq :=  timer_module.io.irq_out

  // pwm IO connections
  io.out :=  Cat(timer_module.io.pwm_h, timer_module.io.pwm_l, 0.U(34.W))

  // QEI module and IO connections
  val QEI = caravalModule(new QuadEncoder with HasPowerPins)
  QEI.wb <> wb
  QEI.io.quadA := io.in(26)
  QEI.io.quadB := io.in(27)

  QEI.io.reg_count_we := IL_module.io.qei_count_we
  QEI.io.reg_count_di := bus_dat_i
  IL_module.io.qei_count_do := QEI.io.reg_count_do

  QEI.io.reg_cfg_we := IL_module.io.qei_cfg_we
  QEI.io.reg_cfg_di := bus_dat_i
  IL_module.io.qei_cfg_do := QEI.io.reg_cfg_do

  IL_module.io.qei_speed_do := QEI.io.reg_speed_do

  // PID module and IO connections
  val PID = caravalModule(new PIDController with HasPowerPins)
  PID.wb <> wb
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

  la.data_out := 0.U(la.data_out.getWidth)
  io.oeb := Cat(Fill(8, true.B), 0.U(30.W))

}
