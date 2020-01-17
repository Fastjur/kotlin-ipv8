package nl.tudelft.ipv8

import io.mockk.mockk
import io.mockk.spyk
import io.mockk.verify
import nl.tudelft.ipv8.keyvault.*
import nl.tudelft.ipv8.messaging.Packet
import nl.tudelft.ipv8.messaging.udp.UdpEndpoint
import nl.tudelft.ipv8.peerdiscovery.Network
import nl.tudelft.ipv8.util.hexToBytes
import nl.tudelft.ipv8.util.toHex
import org.junit.Assert
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import java.net.InetAddress

class CommunityTest {
    private fun getPrivateKey(): PrivateKey {
        val privateKey = "81df0af4c88f274d5228abb894a68906f9e04c902a09c68b9278bf2c7597eaf6"
        val signSeed = "c5c416509d7d262bddfcef421fc5135e0d2bdeb3cb36ae5d0b50321d766f19f2"
        return LibNaClSK(privateKey.hexToBytes(), signSeed.hexToBytes())
    }

    @Test
    fun onPacket() {
        val myPrivateKey = getPrivateKey()
        val myPeer = Peer(myPrivateKey)
        val endpoint = UdpEndpoint(0, InetAddress.getLocalHost())
        val network = Network()

        val community = TestCommunity(myPeer, endpoint, network)
        val handler = mockk<(Address, ByteArray) -> Unit>(relaxed = true)
        community.messageHandlers[246] = handler

        val packet = Packet(myPeer.address, "000260793bdb9cc0b60c96f88069d78aee327a6241d2f6004a4c69624e61434c504b3a7dc013cef4be5e4e051616a9b3cd9c8d8eb5192f037f3104f6323e43d83a934161ef4f7fe7ea4443da306cd998f830cf8bd543525afd929c83d641c7e9ba0ed300000000000000010102030404d20202030408ba030203040ca2010001e3e4862ec5a53c8e44e8bdeffbc8eb21cc441dbe90cc7018d4eb9183bf48d564cd86fe1d5c5d8d2298f7e2b746633ad995e2015597bfa53fb86fb70d1679a104".hexToBytes())
        community.onPacket(packet)

        verify { handler(myPeer.address, any()) }
    }

    @Test
    fun createIntroductionRequest_handleIntroductionRequest() {
        val myPrivateKey = getPrivateKey()
        val myPeer = Peer(myPrivateKey)
        val endpoint = UdpEndpoint(0, InetAddress.getLocalHost())
        val network = Network()

        val community = TestCommunity(myPeer, endpoint, network)

        community.myEstimatedLan = Address("2.2.3.4", 2234)
        community.myEstimatedWan = Address("3.2.3.4", 3234)
        val packet = community.createIntroductionRequest(
            Address("1.2.3.4", 1234)
        )
        community.onPacket(Packet(myPeer.address, packet))
    }

    @Test
    fun createIntroductionRequest() {
        val myPrivateKey = getPrivateKey()
        val myPeer = Peer(myPrivateKey)
        val endpoint = UdpEndpoint(0, InetAddress.getLocalHost())
        val network = Network()

        val community = spyk(TestCommunity(myPeer, endpoint, network))

        community.myEstimatedLan = Address("2.2.3.4", 2234)
        community.myEstimatedWan = Address("3.2.3.4", 3234)
        val packet = community.createIntroductionRequest(
            Address("1.2.3.4", 1234)
        )
        Assert.assertEquals("000260793bdb9cc0b60c96f88069d78aee327a6241d2f6004a4c69624e61434c504b3a7dc013cef4be5e4e051616a9b3cd9c8d8eb5192f037f3104f6323e43d83a934161ef4f7fe7ea4443da306cd998f830cf8bd543525afd929c83d641c7e9ba0ed300000000000000010102030404d20202030408ba030203040ca2010001e3e4862ec5a53c8e44e8bdeffbc8eb21cc441dbe90cc7018d4eb9183bf48d564cd86fe1d5c5d8d2298f7e2b746633ad995e2015597bfa53fb86fb70d1679a104", packet.toHex())
    }

    @Test
    fun createIntroductionResponse() {
        val myPrivateKey = getPrivateKey()

        val myPeer = Peer(myPrivateKey)
        val endpoint = UdpEndpoint(0, InetAddress.getLocalHost())
        val network = Network()

        val community = TestCommunity(myPeer, endpoint, network)

        community.myEstimatedLan = Address("2.2.3.4", 2234)
        community.myEstimatedWan = Address("3.2.3.4", 3234)
        val packet = community.createIntroductionResponse(
            Address("1.2.3.4", 1234),
            Address("1.2.3.4", 1234),
            2,
            introduction = Peer(ECCrypto.generateKey(), Address("5.2.3.4", 5234))
        )

        Assert.assertEquals("000260793bdb9cc0b60c96f88069d78aee327a6241d2f5004a4c69624e61434c504b3a7dc013cef4be5e4e051616a9b3cd9c8d8eb5192f037f3104f6323e43d83a934161ef4f7fe7ea4443da306cd998f830cf8bd543525afd929c83d641c7e9ba0ed300000000000000010102030404d20202030408ba030203040ca200000000000005020304147200000294b144e819f1b3179e8d6117ea7cf7846b4490273999f1bfd2e3c498f9486183be2464cb543003475c5cbff8458ce9e40170d332fad063e238d1dab448098801", packet.toHex())
    }

    @Test
    fun createIntroductionResponse_handleIntroductionResponse() {
        val myPrivateKey = getPrivateKey()

        val myPeer = Peer(myPrivateKey)
        val endpoint = UdpEndpoint(0, InetAddress.getLocalHost())
        val network = Network()

        val community = spyk(TestCommunity(myPeer, endpoint, network))

        community.myEstimatedLan = Address("2.2.3.4", 2234)
        community.myEstimatedWan = Address("3.2.3.4", 3234)
        val packet = community.createIntroductionResponse(
            Address("1.2.3.4", 1234),
            Address("1.2.3.4", 1234),
            2,
            introduction = Peer(ECCrypto.generateKey(), Address("5.2.3.4", 5234))
        )

        community.handleIntroductionResponse(myPeer.address, packet)

        verify { community.onIntroductionResponse(any(), any(), any()) }
    }

    @Test
    fun deserializeIntroductionRequest() {
        // IntroductionRequestPayload(destinationAddress=Address(ip=65472.65448.1.31, port=8090), sourceLanAddress=Address(ip=0.0.0.0, port=0), sourceWanAddress=Address(ip=31.65434.0.0, port=0), advice=true, connectionType=UNKNOWN, identifier=2)
        val receivedPayload = "000260793bdb9cc0b60c96f88069d78aee327a6241d2f6004a4c69624e61434c504b3aa64d93ce5bf38a5340db2ea802e45481831dbbf74e8dda11e9b345af18fd2932d06b2ad334a86c31c654dd23501feb518a259d2f6525d0baa07a9e6194a4dc910000000000000002c0a8011f1f9a000000000000000000000000010002475308c9049daccf949d8f56949f8ba1ca4c2fa1cd225b4674fab87f151f26e37c075dfa9414154ad42a5b000e0a1f487fc0435c75084817b5ba001bd1f61e06"

        val myPrivateKey = getPrivateKey()

        val myPeer = Peer(myPrivateKey)
        val endpoint = UdpEndpoint(0, InetAddress.getLocalHost())
        val network = Network()

        val community = TestCommunity(myPeer, endpoint, network)

        val (peer, dist, payload) = community.deserializeIntroductionRequest(myPeer.address, receivedPayload.hexToBytes())
    }

    @Test
    fun loadAndUnload() {
        val myPrivateKey = getPrivateKey()

        val myPeer = Peer(myPrivateKey)
        val endpoint = UdpEndpoint(0, InetAddress.getLocalHost())
        val network = Network()

        val community = TestCommunity(myPeer, endpoint, network)

        community.load()
        assertEquals(1, network.blacklistMids.size)
        assertEquals(myPeer.mid, network.blacklistMids.first())
        community.unload()
    }

    @Test
    fun bootstrap() {
        val myPrivateKey = getPrivateKey()

        val myPeer = Peer(myPrivateKey)
        val endpoint = UdpEndpoint(0, InetAddress.getLocalHost())
        val network = Network()

        val community = spyk(TestCommunity(myPeer, endpoint, network))

        community.bootstrap()

        verify { community.walkTo(any()) }
    }

    @Test
    fun createPuncture() {
        val myPrivateKey = getPrivateKey()

        val myPeer = Peer(myPrivateKey)
        val endpoint = UdpEndpoint(0, InetAddress.getLocalHost())
        val network = Network()

        val community = spyk(TestCommunity(myPeer, endpoint, network))

        val lanWalker = Address("1.2.3.4", 1234)
        val wanWalker = Address("2.3.4.5", 2345)
        val identifier = 1
        val payload = community.createPuncture(lanWalker, wanWalker, identifier)
        assertEquals("000260793bdb9cc0b60c96f88069d78aee327a6241d2f9004a4c69624e61434c504b3a7dc013cef4be5e4e051616a9b3cd9c8d8eb5192f037f3104f6323e43d83a934161ef4f7fe7ea4443da306cd998f830cf8bd543525afd929c83d641c7e9ba0ed300000000000000010102030404d202030405092900016e45d66684e87a35bddf5d971619dd21de92993639b1021f85be61d940c5ba1cbd943797cfb4058c962d24d0cf19fbd4a7f6ed41e75ea2fe8693a5d876da210f", payload.toHex())
    }

    @Test
    fun handlePuncture() {
        val myPrivateKey = getPrivateKey()

        val myPeer = Peer(myPrivateKey)
        val endpoint = UdpEndpoint(0, InetAddress.getLocalHost())
        val network = Network()

        val community = spyk(TestCommunity(myPeer, endpoint, network))

        val packet = "000260793bdb9cc0b60c96f88069d78aee327a6241d2f9004a4c69624e61434c504b3a7dc013cef4be5e4e051616a9b3cd9c8d8eb5192f037f3104f6323e43d83a934161ef4f7fe7ea4443da306cd998f830cf8bd543525afd929c83d641c7e9ba0ed300000000000000010102030404d202030405092900016e45d66684e87a35bddf5d971619dd21de92993639b1021f85be61d940c5ba1cbd943797cfb4058c962d24d0cf19fbd4a7f6ed41e75ea2fe8693a5d876da210f"
        community.handlePuncture(myPeer.address, packet.hexToBytes())

        verify { community.onPuncture(any(), any(), any()) }
    }

    @Test
    fun createPunctureRequest() {
        val myPrivateKey = getPrivateKey()

        val myPeer = Peer(myPrivateKey)
        val endpoint = UdpEndpoint(0, InetAddress.getLocalHost())
        val network = Network()

        val community = spyk(TestCommunity(myPeer, endpoint, network))

        val lanWalker = Address("1.2.3.4", 1234)
        val wanWalker = Address("2.3.4.5", 2345)
        val identifier = 1
        val packet = community.createPunctureRequest(lanWalker, wanWalker, identifier)
        assertEquals("000260793bdb9cc0b60c96f88069d78aee327a6241d2fa00000000000000010102030404d20203040509290001", packet.toHex())
    }

    @Test
    fun handlePunctureRequest() {
        val myPrivateKey = getPrivateKey()

        val myPeer = Peer(myPrivateKey)
        val endpoint = UdpEndpoint(0, InetAddress.getLocalHost())
        val network = Network()

        val community = spyk(TestCommunity(myPeer, endpoint, network))

        val packet = "000260793bdb9cc0b60c96f88069d78aee327a6241d2fa00000000000000010102030404d202030405092900017b01e303fc9987b1b899445e3f9c3a0208580b3572f357e9667419cf095a8bf5ea7d97f22519695062d7db2a768ad0309afe9cb51607f0a104b623da0235c50e"
        community.handlePunctureRequest(myPeer.address, packet.hexToBytes())

        verify { community.onPunctureRequest(any(), any(), any()) }
    }

    @Test
    fun getNewIntroduction() {
        val myPrivateKey = getPrivateKey()

        val myPeer = Peer(myPrivateKey)
        val endpoint = spyk(UdpEndpoint(0, InetAddress.getLocalHost()))
        val network = Network()
        val community = TestCommunity(myPeer, endpoint, network)

        community.getNewIntroduction()

        verify { endpoint.send(any(), any()) }
    }
}