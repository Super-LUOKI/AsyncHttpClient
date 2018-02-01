package net.callumtaylor.asynchttp

import android.support.test.runner.AndroidJUnit4
import junit.framework.Assert
import net.callumtaylor.asynchttp.obj.Request
import net.callumtaylor.asynchttp.processor.ByteProcessor
import org.junit.Test
import org.junit.runner.RunWith
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

/**
 * // TODO: Add class description
 */
@RunWith(AndroidJUnit4::class)
class AsyncGetTest
{
	/**
	 * Tests the request is properly cancelled and returned instantly
	 */
	@Test
	fun testCancel()
	{
		val signal = CountDownLatch(1)

		AsyncHttpClient("https://httpbin.org/stream-bytes/16384").let { client ->
			client.get<ByteArray>(
				processor = object : ByteProcessor()
				{
					override fun onChunkProcessed(request: Request, length: Long, total: Long)
					{
						if (length > 1024)
						{
							client.cancel()
						}
					}
				},
				response = { response ->
					// cancel code = 0
					Assert.assertEquals(0, response.code)

					// read count < 16384
					Assert.assertTrue(response.length < 16384)

					signal.countDown()
				}
			)
		}

		signal.await(60, TimeUnit.SECONDS)

		if (signal.count != 0L)
		{
			Assert.fail()
		}
	}
}
