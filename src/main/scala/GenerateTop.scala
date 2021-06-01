import chisel3.stage.{ChiselGeneratorAnnotation, ChiselStage}
import subsystems.MotorTop

object GenerateTop {
  def main(args: Array[String]): Unit = {
    (new ChiselStage).execute(
      Array("-X", "verilog"),
      Seq(ChiselGeneratorAnnotation(() => new MotorTop())))
  }
}
