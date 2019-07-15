package tracegen

import chisel3._
import chisel3.util.log2Ceil
import freechips.rocketchip.config.{Config, Parameters}
import freechips.rocketchip.groundtest.{GroundTestTilesKey, TraceGenParams}
import freechips.rocketchip.subsystem.{ExtMem, SystemBusKey}
import freechips.rocketchip.system.BaseConfig
import freechips.rocketchip.rocket.DCacheParams
import freechips.rocketchip.tile.{MaxHartIdBits, XLen}
import scala.math.min

class WithTraceGen(params: Seq[DCacheParams], nReqs: Int = 8192)
    extends Config((site, here, up) => {
  case GroundTestTilesKey => params.map { dcp => TraceGenParams(
    dcache = Some(dcp),
    wordBits = site(XLen),
    addrBits = 48,
    addrBag = {
      val nSets = dcp.nSets
      val nWays = dcp.nWays
      val blockOffset = site(SystemBusKey).blockOffset
      val nBeats = min(2, site(SystemBusKey).blockBeats)
      val beatBytes = site(SystemBusKey).beatBytes
      List.tabulate(nWays) { i =>
        Seq.tabulate(nBeats) { j => BigInt((j * beatBytes) + ((i * nSets) << blockOffset)) }
      }.flatten
    },
    maxRequests = nReqs,
    memStart = site(ExtMem).get.master.base,
    numGens = params.size)
  }
  case MaxHartIdBits => if (params.size == 1) 1 else log2Ceil(params.size)
})

class NonBlockingTraceGenConfig extends Config(
  new WithTraceGen(List.fill(2) { DCacheParams(nMSHRs = 2, nSets = 16, nWays = 2) }) ++
  new BaseConfig)
