package MC_Module

import chisel3._
import chisel3.util.Cat

class PWM_IO extends Bundle {

  val reg_val_we =  Input(Bool())
  val reg_val_di =  Input(UInt(32.W))
  val reg_val_do =  Output(UInt(32.W))
  val reg_cfg_we =  Input(Bool())
  val reg_cfg_di =  Input(UInt(32.W))
  val reg_cfg_do =  Output(UInt(32.W))
  val reg_dat_we =  Input(Bool())
  val reg_dat_di =  Input(UInt(32.W))
  val reg_dat_do =  Output(UInt(32.W))
  val reg_duty_we =  Input(Bool())
  val reg_duty_di =  Input(UInt(32.W))
  val reg_duty_do =  Output(UInt(32.W))

  val reg_pid_out = Input(SInt(16.W))

  val pwm_h = Output(Bool())
  val pwm_l = Output(Bool())
 // val stop_out = Output(Bool())
 // val is_offset = Output(Bool())
  val irq_out = Output(Bool())
  val rawirq_out = Output(Bool())
}

class PWM extends Module {

  // IO module
  val io = IO(new PWM_IO)

  // Timer internal registers and their initialization
  val value_cur = RegInit(UInt(32.W), 0.U)
  val value_reset = RegInit(UInt(32.W), 10.U)
  val pwm_duty = RegInit(UInt(32.W), 0.U)

  val value_cur_plus = Wire(UInt(32.W))  // Next value, on up-count
  val value_cur_minus = Wire(UInt(32.W))  // Next value, on down-count

  val loc_enable = Wire(Bool()) 
  val enable = RegInit(Bool(), false.B) // Enable (start) the counter/timer

  val stopOut = RegInit(Bool(), false.B)
  val irqOut = RegInit(Bool(), false.B)

  // Previous state of enable (catch rising/falling edge)
  val lastenable = Reg( Bool())

  // Count up (1) or down (0)
  val updown = RegInit(Bool(), false.B)

  // Enable interrupt on timeout
  val irq_ena = RegInit(Bool(), false.B)

  // Enable PID output to drive timer duty cycle
  val pid_out_sel = RegInit(Bool(), false.B)

  // PWM deadband configuration bit field
  val pwm_db = RegInit(UInt(4.W), 2.U)

  // PWM implementation and duty_cycle register read/write
  io.reg_duty_do := pwm_duty

  when(io.reg_duty_we) {
    pwm_duty := io.reg_duty_di
  }.elsewhen(pid_out_sel){
    pwm_duty := io.reg_pid_out.asUInt()
  }

  val proc_offset = Reg(UInt(32.W))
  val pwm_ld = Reg(Bool())
  val pwm_hd = Reg(Bool())

  // Limit the offset internally to keep the deadband from being over written
  val pwm_db_twice = (pwm_db << 1.U).asUInt()

  proc_offset := Mux((pwm_duty >= pwm_db_twice) && (pwm_duty <= (value_reset - pwm_db_twice)), pwm_duty,
                      Mux(pwm_duty < pwm_db_twice, pwm_db_twice, (value_reset - pwm_db_twice)))
  pwm_hd := value_cur < proc_offset - pwm_db // on initially and turn off a DB width prior to offset
  pwm_ld := value_cur > proc_offset && value_cur < value_reset - pwm_db // on at offset and off DW width prior to max count;

  io.pwm_h := pwm_hd
  io.pwm_l := pwm_ld

  io.irq_out := irqOut
  io.rawirq_out := stopOut & ~irqOut

  // Configuration register
  io.reg_cfg_do := Cat(0.U(24.W), pwm_db(3,0), pid_out_sel, irq_ena, updown, enable)

  when(io.reg_cfg_we) {
    enable := io.reg_cfg_di(0)
    updown := io.reg_cfg_di(1)
    irq_ena := io.reg_cfg_di(2)
    pid_out_sel := io.reg_cfg_di(3)
    pwm_db := io.reg_cfg_di(7,4)
  }

// Counter/timer reset value register
  io.reg_val_do := value_reset

  when(io.reg_val_we.orR()) {
    value_reset := io.reg_val_di
  }

  // Counter/timer current value register and timer implementation
  io.reg_dat_do := value_cur

  value_cur_plus := value_cur.asUInt + 1.U
  value_cur_minus := value_cur.asUInt - 1.U
  loc_enable :=  enable

// Timer count register up-dation and IRQ generation

  lastenable := loc_enable
  when(io.reg_dat_we.orR()) {
      value_cur := io.reg_dat_di
  } .elsewhen (loc_enable === true.B) {

    /* IRQ signals one cycle after stop_out, if IRQ is enabled
     IRQ lasts for one clock cycle only.	*/

    irqOut := Mux((irq_ena), (stopOut & ~irqOut), "b0".U(1.W))


      when(updown === true.B) {
        when(lastenable === false.B) {
          value_cur := 0.U(32.W)
        //  strobeOut := false.B
          stopOut := false.B
        } .otherwise { // count up

      // Single 32-bit counter behavior
          when(value_cur.asUInt === value_reset.asUInt) {
            value_cur := 0.U(32.W)
            stopOut := true.B
          } .otherwise {
            value_cur := value_cur_plus
            stopOut := false.B
          }
        }
      } .otherwise { // count down

        when(lastenable === false.B) {
          value_cur := value_reset
           stopOut := false.B
        //  strobeOut := false.B
        } .otherwise { // count down

      // Single 32-bit counter countdown behavior
          when(value_cur.asUInt === 0.U(32.W)) {
            value_cur := value_reset
            stopOut := true.B
            }.otherwise {
              value_cur := value_cur_minus
              stopOut := false.B
            }
        }
      }
    }
}

object Timer_generate extends App {
  //(new chisel3.stage.ChiselStage).execute(args, () => new quad)

  chisel3.Driver.execute(args, () => new PWM)
}