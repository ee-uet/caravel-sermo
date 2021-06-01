package MC_Module

import chisel3._
//import sv2chisel.helpers.vecconvert._
import chisel3.util.Cat
//import chisel3.stage.{ChiselStage, ChiselGeneratorAnnotation}

class vedic2IO extends Bundle {
  val a = Input(UInt(2.W))
  val b = Input(UInt(2.W))
  val c = Output(UInt(4.W))

}

class vedic_2x2 extends Module {
 // val reset = IO(Input(Bool()))
 val io = IO(new vedic2IO)



    //stage 1
    // four multiplication operation of bits accourding to vedic logic done using and gates

  val result0 = io.a(0) & io.b(0)
  val temp0 = io.a(1) & io.b(0)
  val temp1 = io.a(0) & io.b(1)
  val temp2 = io.a(1) & io.b(1)


    //stage two
    // using two half adders
  val result1 = temp0 ^ temp1
  val temp3 = temp0 & temp1

  val result2 = temp2 ^ temp3
  val result3 = temp2 & temp3

  io.c := Cat(result3, result2, result1, result0)
}

class vedic4IO extends Bundle {
  val a = Input(UInt(4.W))
  val b = Input(UInt(4.W))
  val c = Output(UInt(8.W))

}

class vedic_4x4 extends Module {
  // val reset = IO(Input(Bool()))
  val io = IO(new vedic4IO)

    val q0 = Wire(UInt(4.W))
    val q1 = Wire(UInt(4.W))
    val q2 = Wire(UInt(4.W))
    val q3 = Wire(UInt(4.W))

    val temp1 = Wire(UInt(4.W))
    val temp2 = Wire(UInt(6.W))
    val temp3 = Wire(UInt(6.W))
    val temp4 = Wire(UInt(6.W))

    val q4 = Wire(UInt(4.W))
    val q5 = Wire(UInt(6.W))
    val q6 = Wire(UInt(6.W))


    // using 4 2x2 multipliers
    val z1 = Module(new vedic_2x2) // using 4 2x2 multipliers

    z1.io.a := io.a(1,0)
    z1.io.b := io.b(1,0)
    q0 := z1.io.c
    val z2 = Module(new vedic_2x2)
    z2.io.a := io.a(3,2)
    z2.io.b := io.b(1,0)
    q1 := z2.io.c
    val z3 = Module(new vedic_2x2)
    z3.io.a := io.a(1,0)
    z3.io.b := io.b(3,2)
    q2 := z3.io.c
    val z4 = Module(new vedic_2x2)
    z4.io.a := io.a(3,2)
    z4.io.b := io.b(3,2)
    q3 := z4.io.c


    // stage 1 adders
    temp1 := Cat("b0".U(2.W), q0(3,2).asUInt)
    q4 := q1(3,0) + temp1
    temp2 := Cat("b0".U(2.W), q2(3,0))
    temp3 := Cat(q3(3,0).asUInt, "b0".U(2.W))
    q5 := temp2 + temp3
    temp4 := Cat("b0".U(2.W), q4(3,0).asUInt) // stage 2 adder

    q6 := temp4 + q5 // final output assignment

  val result1 = q0(1,0)
  val result2 = q6(5,0)

  io.c:= Cat(result2, result1)

}

class vedic8IO extends Bundle {
  val a = Input(SInt(9.W))
  val b = Input(UInt(8.W))
  val c = Output(SInt(16.W))

}

class vedic_8x8 extends Module {
  // val reset = IO(Input(Bool()))
  val io = IO(new vedic8IO)

  val q0 = Wire(UInt(16.W))
  val q1 = Wire(UInt(16.W))
  val q2 = Wire(UInt(16.W))
  val q3 = Wire(UInt(16.W))

  val temp1 = Wire(UInt(8.W))
  val temp2 = Wire(UInt(12.W))
  val temp3 = Wire(UInt(12.W))
  val temp4 = Wire(UInt(12.W))

  val q4 = Wire(UInt(8.W))
  val q5 = Wire(UInt(12.W))
  val q6 = Wire(UInt(12.W))

  val in1_complement = (~io.a).asUInt() + 1.U
  val input1 = Mux(io.a(8), in1_complement(7,0), io.a(7,0).asUInt())

  // using 4 2x2 multipliers
  val z1 = Module(new vedic_4x4) // using 4 2x2 multipliers

  z1.io.a := input1(3,0)
  z1.io.b := io.b(3,0)
  q0 := z1.io.c
  val z2 = Module(new vedic_4x4)
  z2.io.a := input1(7,4)
  z2.io.b := io.b(3,0)
  q1 := z2.io.c
  val z3 = Module(new vedic_4x4)
  z3.io.a := input1(3,0)
  z3.io.b := io.b(7,4)
  q2 := z3.io.c
  val z4 = Module(new vedic_4x4)
  z4.io.a := input1(7,4)
  z4.io.b := io.b(7,4)
  q3 := z4.io.c


  // stage 1 adders
  temp1 := Cat("b0".U(4.W), q0(7,4).asUInt)
  q4 := q1(7,0) + temp1
  temp2 := Cat("b0".U(4.W), q2(7,0))
  temp3 := Cat(q3(7,0).asUInt, "b0".U(4.W))
  q5 := temp2 + temp3
  temp4 := Cat("b0".U(4.W), q4(7,0).asUInt) // stage 2 adder

  q6 := temp4 + q5 // final output assignment

  val result1 = q0(3,0)
  val result2 = q6(11,0)

  val result = Cat(result2, result1)
  val result_complement = (~result).asSInt() + 1.S

  val result_final = Mux(io.a(8), result_complement, result.asSInt())

  io.c:= result_final
}

object vedic_8x8_generate extends App {
  //(new chisel3.stage.ChiselStage).execute(args, () => new quad)

  chisel3.Driver.execute(args, () => new vedic_8x8)

}