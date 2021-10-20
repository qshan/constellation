package constellation

import chisel3._
import chisel3.util._

import freechips.rocketchip.config.{Field, Parameters}


class RouteComputerReq(val cParam: ChannelParams)(implicit val p: Parameters) extends Bundle with HasChannelParams {
  val src_virt_id = UInt(virtualChannelBits.W)
  val src_prio = UInt(prioBits.W)
  val dest_id = UInt(nodeIdBits.W)
}

class RouteComputerResp(val cParam: ChannelParams, val nOutputs: Int)(implicit val p: Parameters) extends Bundle with HasChannelParams {
  val src_virt_id = UInt(virtualChannelBits.W)
  val out_channels = UInt(nOutputs.W)
}



class RouteComputer(val rParams: RouterParams)(implicit val p: Parameters) extends Module with HasRouterParams {
  val io = IO(new Bundle {
    val req = MixedVec(allInParams.map { u => Flipped(Decoupled(new RouteComputerReq(u))) })
    val resp = MixedVec(allInParams.map { u => Valid(new RouteComputerResp(u, nOutputs)) })
  })
  val routingMatrix = outParams.map { outP => allInParams.map { inP =>
    val mat = Wire(Vec(nNodes, Vec(nPrios, Bool())))
    Seq.tabulate(nNodes, nPrios) { case (dst,prio) =>
      mat(dst)(prio) := routingFunction(inP.srcId, dst, outP.destId, prio).B
    }
    mat
  }}

  (io.req zip io.resp).zipWithIndex.map { case ((req, resp), i) =>
    req.ready := true.B
    resp.valid := req.valid
    resp.bits.src_virt_id := req.bits.src_virt_id
    if (outParams.size == 0) {
      assert(!req.valid)
      resp.bits.out_channels := 0.U
    } else {
      resp.bits.out_channels := VecInit(
        routingMatrix.map(_(i)(req.bits.dest_id)(req.bits.src_prio))
      ).asUInt
    }
  }
}
