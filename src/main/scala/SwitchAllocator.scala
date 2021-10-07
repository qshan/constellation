package astronoc

import chisel3._
import chisel3.util._

import freechips.rocketchip.config.{Field, Parameters}
import freechips.rocketchip.util._


class SwitchAllocReq(val nOutputs: Int)(implicit val p: Parameters) extends Bundle with HasAstroNoCParams {
  val out_channel = UInt(log2Ceil(nOutputs).W)
}

class SwitchAllocator(val rParams: RouterParams)(implicit val p: Parameters) extends Module with HasRouterParams {
  val io = IO(new Bundle {
    val req = MixedVec(inParams.map(u => Vec(u.virtualChannelParams.size, Flipped(Decoupled(new SwitchAllocReq(outParams.size))))))
    val credit_alloc = MixedVec(outParams.map { u => Valid(UInt(log2Ceil(u.virtualChannelParams.size).W)) })
  })
  io := DontCare
}
