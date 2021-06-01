package subsystems

import chisel3._
import chisel3.util.Cat

trait Config {
  // Timer register addresses
  val TM_BASE_ADR = "h3000_0000".U(32.W)
  val TM_CONFIG = "h00".U(8.W)
  val TM_VALUE = "h04".U(8.W)
  val TM_DATA = "h08".U(8.W)
  val TM_DUTY = "h0C".U(8.W)

  // QEI register addresses
  val QEI_BASE_ADR = "h3000_0100".U(32.W)
  val QEI_COUNT =    "h00".U(8.W)
  val QEI_SPEED =    "h04".U(8.W)
  val QEI_CFG =    "h08".U(8.W)

  // PID controller register addresses
  val PID_BASE_ADR = "h3000_0200".U(32.W)
  val PID_KP =    "h00".U(8.W)
  val PID_KI =    "h04".U(8.W)
  val PID_KD =    "h08".U(8.W)
  val PID_REF =    "h0C".U(8.W)
  val PID_FB =    "h0F".U(8.W)
  val PID_CFG =    "h10".U(8.W)
}

class InterLinkIO extends Bundle {
  // Wishbone bus signals
  val bus_adr_i =  Input(UInt(32.W))
  val bus_sel_i =  Input(UInt(4.W))
  val bus_we_i =  Input(Bool())
  val bus_cyc_i =  Input(Bool())
  val bus_stb_i =  Input(Bool())
  val bus_ack_o =  Output(Bool())
  val bus_dat_o =  Output(UInt(32.W))

  // IOs for timer module
  val tmr_val_we =  Output(Bool())
  val tmr_val_do =  Input(UInt(32.W))
  val tmr_dat_we =  Output(Bool())
  val tmr_dat_do =  Input(UInt(32.W))
  val tmr_duty_we =  Output(Bool())
  val tmr_duty_do =  Input(UInt(32.W))
  val tmr_cfg_we =  Output(Bool())
  val tmr_cfg_do =  Input(UInt(32.W))

  val qei_count_we =  Output(Bool())
  val qei_count_do =  Input(UInt(32.W))
  val qei_cfg_we =  Output(Bool())
  val qei_cfg_do =  Input(UInt(32.W))
  val qei_speed_do =  Input(SInt(16.W))

  val pid_kp_we =  Output(Bool())
  val pid_kp_do =  Input(SInt(16.W))
  val pid_ki_we =  Output(Bool())
  val pid_ki_do =  Input(SInt(16.W))
  val pid_kd_we =  Output(Bool())
  val pid_kd_do =  Input(SInt(16.W))
  val pid_ref_we =  Output(Bool())
  val pid_ref_do =  Input(SInt(16.W))
  val pid_fb_we =  Output(Bool())
  val pid_fb_do =  Input(SInt(16.W))
  val pid_cfg_we =  Output(Bool())
  val pid_cfg_do =  Input(SInt(16.W))
}


class InterlinkModule extends RawModule with Config{

  val io = IO(new InterLinkIO)

  override def desiredName: String = "InterlinkModule"

   val bus_valid = WireInit(Bool(), io.bus_stb_i && io.bus_cyc_i)

  // timer module register read/write related definitions
  val tmr_cfg_do = Wire(UInt(32.W))
  val tmr_val_do = Wire(UInt(32.W))
  val tmr_dat_do = Wire(UInt(32.W))
  val tmr_duty_do = Wire(UInt(32.W))

  val tmr_cfg_sel = WireInit(Bool(), bus_valid && (io.bus_adr_i === (TM_BASE_ADR|TM_CONFIG)))
  val tmr_val_sel = WireInit(Bool(), bus_valid && (io.bus_adr_i === (TM_BASE_ADR|TM_VALUE)))
  val tmr_dat_sel = WireInit(Bool(), bus_valid && (io.bus_adr_i === (TM_BASE_ADR|TM_DATA)))
  val tmr_duty_sel = WireInit(Bool(), bus_valid && (io.bus_adr_i === (TM_BASE_ADR|TM_DUTY)))

  val tmr_cfg_we = WireInit(Bool(), Mux((tmr_cfg_sel), (io.bus_sel_i(0) & Cat(io.bus_we_i)), "b0".U(1.W)))
  val tmr_val_we = WireInit(Bool(), Mux((tmr_val_sel), (io.bus_sel_i(0) & Cat(io.bus_we_i)), "b0".U(1.W)))
  val tmr_dat_we = WireInit(Bool(), Mux((tmr_dat_sel), (io.bus_sel_i(0) & Cat(io.bus_we_i)), "b0".U(1.W)))
  val tmr_duty_we = WireInit(Bool(), Mux((tmr_duty_sel), (io.bus_sel_i(0) & Cat(io.bus_we_i)), "b0".U(1.W)))

  val reg_dat_re = WireInit(Bool(), tmr_dat_sel &&  !(io.bus_sel_i =/= 0.U) &&  ~io.bus_we_i)

  val tmr_sel = tmr_cfg_sel || tmr_val_sel || tmr_dat_sel || tmr_duty_sel
  val tmr_do = Mux((tmr_cfg_sel), tmr_cfg_do, Mux((tmr_val_sel), tmr_val_do,
                     Mux(tmr_duty_sel, tmr_duty_do, tmr_dat_do)))
 //val tm_reg_do = Mux((tm_reg_cfg_sel), tm_reg_cfg_do, Mux((tm_reg_val_sel), tm_reg_val_do,  tm_reg_dat_do))


  val qei_count_do = Wire(UInt(32.W))
  val qei_count_sel = WireInit(Bool(), bus_valid && (io.bus_adr_i === (QEI_BASE_ADR|QEI_COUNT)))
  val qei_count_we = WireInit(Bool(), Mux((qei_count_sel), (io.bus_sel_i(0) & Cat(io.bus_we_i)), "b0".U(1.W)))

  val qei_cfg_do = Wire(UInt(32.W))
  val qei_cfg_sel = WireInit(Bool(), bus_valid && (io.bus_adr_i === (QEI_BASE_ADR|QEI_CFG)))
  val qei_cfg_we = WireInit(Bool(), Mux((qei_cfg_sel), (io.bus_sel_i(0) & Cat(io.bus_we_i)), "b0".U(1.W)))

  val qei_speed_do = Wire(UInt(32.W))
  val qei_speed_sel = WireInit(Bool(), bus_valid && (io.bus_adr_i === (QEI_BASE_ADR|QEI_SPEED)))
  val qei_speed_we = WireInit(Bool(), Mux((qei_speed_sel), (io.bus_sel_i(0) & Cat(io.bus_we_i)), "b0".U(1.W)))


 // io.Speed := Speed
  val qei_sel = qei_count_sel || qei_cfg_sel || qei_speed_sel
  val qei_do = Mux(qei_speed_sel, qei_speed_do, Mux(qei_cfg_sel, qei_cfg_do, qei_count_do))

  // PID module and IO connections

  val pid_kp_do = Wire(SInt(16.W))
  val pid_kp_sel = WireInit(Bool(), bus_valid && (io.bus_adr_i === (PID_BASE_ADR|PID_KP)))
  val pid_kp_we = WireInit(Bool(), Mux((pid_kp_sel), (io.bus_sel_i(0) & Cat(io.bus_we_i)), "b0".U(1.W)))

  val pid_ki_do = Wire(SInt(16.W))
  val pid_ki_sel = WireInit(Bool(), bus_valid && (io.bus_adr_i === (PID_BASE_ADR|PID_KI)))
  val pid_ki_we = WireInit(Bool(), Mux((pid_ki_sel), (io.bus_sel_i(0) & Cat(io.bus_we_i)), "b0".U(1.W)))

  val pid_kd_do = Wire(SInt(16.W))
  val pid_kd_sel = WireInit(Bool(), bus_valid && (io.bus_adr_i === (PID_BASE_ADR|PID_KD)))
  val pid_kd_we = WireInit(Bool(), Mux((pid_kd_sel), (io.bus_sel_i(0) & Cat(io.bus_we_i)), "b0".U(1.W)))

  val pid_ref_do = Wire(SInt(16.W))
  val pid_ref_sel = WireInit(Bool(), bus_valid && (io.bus_adr_i === (PID_BASE_ADR|PID_REF)))
  val pid_ref_we = WireInit(Bool(), Mux((pid_ref_sel), (io.bus_sel_i(0) & Cat(io.bus_we_i)), "b0".U(1.W)))

  val pid_fb_do = Wire(SInt(16.W))
  val pid_fb_sel = WireInit(Bool(), bus_valid && (io.bus_adr_i === (PID_BASE_ADR|PID_FB)))
  val pid_fb_we = WireInit(Bool(), Mux((pid_fb_sel), (io.bus_sel_i(0) & Cat(io.bus_we_i)), "b0".U(1.W)))

  val pid_cfg_do = Wire(SInt(16.W))
  val pid_cfg_sel = WireInit(Bool(), bus_valid && (io.bus_adr_i === (PID_BASE_ADR|PID_CFG)))
  val pid_cfg_we = WireInit(Bool(), Mux((pid_cfg_sel), (io.bus_sel_i(0) & Cat(io.bus_we_i)), "b0".U(1.W)))

  val pid_sel = pid_kp_sel || pid_ki_sel || pid_kd_sel || pid_ref_sel || pid_fb_sel || pid_cfg_sel
  val pid_do = Mux(pid_kp_sel, pid_kp_do, Mux(pid_ki_sel, pid_ki_do,
                                          Mux(pid_kd_sel, pid_kd_do,
                                          Mux(pid_ref_sel, pid_ref_do,
                                          Mux(pid_fb_sel, pid_fb_do, pid_cfg_do)))))

  // update the WB output signals
  io.bus_dat_o := Mux(tmr_sel, tmr_do, Mux(qei_sel, qei_do, Mux(pid_sel, pid_do.asUInt(), 0.U)))
  io.bus_ack_o := tmr_sel || qei_sel || pid_sel

  // IO wiring for different modules
  io.tmr_val_we := tmr_val_we
  tmr_val_do := io.tmr_val_do
  io.tmr_dat_we := tmr_dat_we
  tmr_dat_do := io.tmr_dat_do
  io.tmr_duty_we := tmr_duty_we
  tmr_duty_do := io.tmr_duty_do
  io.tmr_cfg_we := tmr_cfg_we
  tmr_cfg_do := io.tmr_cfg_do

  io.qei_count_we := qei_count_we
  qei_count_do := io.qei_count_do
  io.qei_cfg_we := qei_cfg_we
  qei_cfg_do := io.qei_cfg_do
 // io.qei_speed_we := qei_speed_we
  qei_speed_do := io.qei_speed_do.asUInt()

  io.pid_kp_we := pid_kp_we
  pid_kp_do := io.pid_kp_do
  io.pid_ki_we := pid_ki_we
  pid_ki_do := io.pid_ki_do
  io.pid_kd_we := pid_kd_we
  pid_kd_do := io.pid_kd_do
  io.pid_ref_we := pid_ref_we
  pid_ref_do := io.pid_ref_do
  io.pid_fb_we := pid_fb_we
  pid_fb_do := io.pid_fb_do
  io.pid_cfg_we := pid_cfg_we
  pid_cfg_do := io.pid_cfg_do
}