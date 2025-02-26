package com.tencent.liteav.debug;

import android.text.TextUtils;
import android.util.Base64;

import org.json.JSONException;
import org.json.JSONObject;

import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.zip.Deflater;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

/*
 *
 *
 * Description: Generates UserSig for testing. UserSig is a security signature designed
 *           by Tencent Cloud for its cloud services.
 *           It is calculated based on `SDKAppID`, `UserID`,
 *           and `EXPIRETIME` using the HMAC-SHA256 encryption algorithm.
 *
 * Attention: For the following reasons, do not use the code below in your commercial application.
 *
 *            The code may be able to calculate UserSig correctly, b
 *            ut it is only for quick testing of the SDK’s basic features,
 *            not for commercial applications.
 *            `SECRETKEY` in client code can be easily decompiled and reversed, especially on web.
 *            Once your key is disclosed, attackers will be able to steal your Tencent Cloud traffic.
 *
 *            The correct method is to deploy the `UserSig` calculation code and encryption key on your server
 *            so that your application can request a `UserSig` from your server,
 *            which will calculate it whenever one is needed.
 *            Given that it is more difficult to hack a server than a client application,
 *            server-end calculation can better protect your key.
 *
 * Reference: https://cloud.tencent.com/document/product/269/32688#Server
 */
public class GenerateTestUserSig {

    /**
     * Tencent Cloud SDKAppID. Set it to the SDKAppID of your account.
     * <p>
     * You can view your `SDKAppId` after creating an application in the [Tencent Cloud IM console](https://console.cloud.tencent.com/avc).
     * SDKAppID uniquely identifies a Tencent Cloud account.
     */
    public static final int SDKAPPID = 1600004380;


    /**
     * Signature validity period, which should not be set too short
     * <p>
     * Unit: Second
     * Default value: 604800 (seven days)
     */
    private static final int EXPIRETIME = 604800;

    /**
     * Sends request for the pull/push address from the backend
     * <p>
     * key: url_push         Gets the RTMP push address
     * key: url_play_flv     Gets the FLV playback address
     */
    public static final String URL_FETCH_PUSH_URL = "https://service-lqp6ki6e-1257335989.gz.apigw.tencentcs.com/release/";

    /**
     * Follow the steps below to obtain the key required for UserSig calculation.
     * <p>
     * Step 1. Log in to the [IM console](https://console.cloud.tencent.com/avc). If you don't have an application yet, create one.
     * Step 2. Click your application and find “Basic Information”.
     * Step 3. Click “Display Key” to view the key used for UserSig calculation.
     * Copy and paste the key to the variable below.
     * <p>
     * Note: This method is for testing only. Before commercial launch,
     * please migrate the UserSig calculation code and key to your backend server
     * to prevent key disclosure and traffic stealing.
     * Documentation: https://cloud.tencent.com/document/product/269/32688#Server
     */
    private static final String SECRETKEY = "2cd6a415a3308e344be42e89e856786e87e482260149736abe5725862d5b29bd";


    /**
     * Xmagic美颜证书url
     * <p>
     * 详细内容见https://cloud.tencent.com/document/product/616/65878
     * </p>
     */
    public static final String XMAGIC_LICENSE_URL = "PLACEHOLDER";

    /**
     * Xmagic美颜证书url
     * <p>
     * 详细内容见https://cloud.tencent.com/document/product/616/65878
     * </p>
     */
    public static final String XMAGIC_LICENSE_KEY = "PLACEHOLDER";


    /**
     * 腾讯云直播license管理页面(https://console.cloud.tencent.com/live/license)
     * 当前应用的License LicenseUrl
     * <p>
     * License Management View (https://console.cloud.tencent.com/live/license)
     * License URL of your application
     */
    public static final String LICENSEURL = "https://license.vod2.myqcloud.com/license/v2/1257335989_1/v_cube.license";

    /**
     * 腾讯云直播license管理页面(https://console.cloud.tencent.com/live/license)
     * 当前应用的License Key
     * <p>
     * License Management View (https://console.cloud.tencent.com/live/license)
     * License key of your application
     */
    public static final String LICENSEURLKEY = "690f65f0ba08ff7937fefbaf15021d8e";

    /**
     * 配置的后台服务域名，类似：https://service-3vscss6c-xxxxxxxxxxx.gz.apigw.tencentcs.com"
     * <p>
     * 小直播后台提供有登录、房间列表等服务，更多细节见文档：https://cloud.tencent.com/document/product/454/38625
     */
    public static final String SERVERLESSURL = "https://service-lqp6ki6e-1257335989.gz.apigw.tencentcs.com/release/";

    /**
     * 配置的播放域名
     * <p>
     * 腾讯云域名管理页面：https://console.cloud.tencent.com/live/domainmanage
     */
    public static final String PLAY_DOMAIN = "192341.push.tlivecloud.com";

    /**
     * Calculating UserSig
     * <p>
     * The asymmetric encryption algorithm HMAC-SHA256 is used in the
     * function to calculate UserSig based on `SDKAppID`, `UserID`, and `EXPIRETIME`.
     *
     * @note: For the following reasons, do not use the code below in your commercial application.
     * <p>
     * The code may be able to calculate UserSig correctly,
     * but it is only for quick testing of the SDK’s basic features,
     * not for commercial applications.
     * SECRETKEY in client code can be easily decompiled and reversed, especially on web.
     * Once your key is disclosed, attackers will be able to steal your Tencent Cloud traffic.
     * <p>
     * The correct method is to deploy the `UserSig` calculation code and encryption key on your server
     * so that your application can request a `UserSig` from your server,
     * which will calculate it whenever one is needed.
     * Given that it is more difficult to hack a server than a client application,
     * server-end calculation can better protect your key.
     * <p>
     * Documentation: https://cloud.tencent.com/document/product/269/32688#Server
     */
    public static String genTestUserSig(String userId) {
        return genTLSSignature(SDKAPPID, userId, EXPIRETIME, null, SECRETKEY);
    }

    /**
     * Generating a TLS Ticket
     *
     * @param sdkappid      `appid` of your application
     * @param userId        User ID
     * @param expire        Validity period in seconds
     * @param userbuf       `null` by default
     * @param priKeyContent Private key required for generating a TLS ticket
     * @return If an error occurs, an empty string will be returned or exceptions printed.
     * If the operation succeeds, a valid ticket will be returned.
     */
    private static String genTLSSignature(long sdkappid, String userId,
                                          long expire, byte[] userbuf, String priKeyContent) {
        if (TextUtils.isEmpty(priKeyContent)) {
            return "";
        }
        long currTime = System.currentTimeMillis() / 1000;
        JSONObject sigDoc = new JSONObject();
        try {
            sigDoc.put("TLS.ver", "2.0");
            sigDoc.put("TLS.identifier", userId);
            sigDoc.put("TLS.sdkappid", sdkappid);
            sigDoc.put("TLS.expire", expire);
            sigDoc.put("TLS.time", currTime);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        String base64UserBuf = null;
        if (null != userbuf) {
            base64UserBuf = Base64.encodeToString(userbuf, Base64.NO_WRAP);
            try {
                sigDoc.put("TLS.userbuf", base64UserBuf);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        String sig = hmacsha256(sdkappid, userId, currTime, expire, priKeyContent, base64UserBuf);
        if (sig.length() == 0) {
            return "";
        }
        try {
            sigDoc.put("TLS.sig", sig);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Deflater compressor = new Deflater();
        compressor.setInput(sigDoc.toString().getBytes(Charset.forName("UTF-8")));
        compressor.finish();
        byte[] compressedBytes = new byte[2048];
        int compressedBytesLength = compressor.deflate(compressedBytes);
        compressor.end();
        return new String(base64EncodeUrl(Arrays.copyOfRange(compressedBytes, 0, compressedBytesLength)));
    }


    private static String hmacsha256(long sdkappid, String userId, long currTime,
                                     long expire, String priKeyContent, String base64Userbuf) {
        String contentToBeSigned = "TLS.identifier:" + userId + "\n"
                + "TLS.sdkappid:" + sdkappid + "\n"
                + "TLS.time:" + currTime + "\n"
                + "TLS.expire:" + expire + "\n";
        if (null != base64Userbuf) {
            contentToBeSigned += "TLS.userbuf:" + base64Userbuf + "\n";
        }
        try {
            byte[] byteKey = priKeyContent.getBytes("UTF-8");
            Mac hmac = Mac.getInstance("HmacSHA256");
            SecretKeySpec keySpec = new SecretKeySpec(byteKey, "HmacSHA256");
            hmac.init(keySpec);
            byte[] byteSig = hmac.doFinal(contentToBeSigned.getBytes("UTF-8"));
            return new String(Base64.encode(byteSig, Base64.NO_WRAP));
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    private static byte[] base64EncodeUrl(byte[] input) {
        byte[] base64 = new String(Base64.encode(input, Base64.NO_WRAP)).getBytes();
        for (int i = 0; i < base64.length; ++i) {
            switch (base64[i]) {
                case '+':
                    base64[i] = '*';
                    break;
                case '/':
                    base64[i] = '-';
                    break;
                case '=':
                    base64[i] = '_';
                    break;
                default:
                    break;
            }
        }
        return base64;
    }

}
