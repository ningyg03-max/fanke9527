package com.salaryjump.app;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.URL;

/* loaded from: classes.dex */
public final class NetDiag {
    private NetDiag() {
    }

    public static String run(Context context) {
        boolean z;
        StringBuilder sb = new StringBuilder();
        try {
            ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService("connectivity");
            NetworkInfo activeNetworkInfo = connectivityManager == null ? null : connectivityManager.getActiveNetworkInfo();
            if (activeNetworkInfo != null) {
                sb.append("0) 设备网络：").append(activeNetworkInfo.getTypeName()).append(activeNetworkInfo.isConnected() ? " 已连接" : " 未连接").append('\n');
            } else {
                sb.append("0) 设备网络：无法获取（可能无网络）\n");
            }
        } catch (Throwable th) {
            sb.append("0) 设备网络：读取失败 ").append(shortName(th)).append('\n');
        }
        long currentTimeMillis = System.currentTimeMillis();
        try {
            InetAddress[] allByName = InetAddress.getAllByName("cdn.jsdelivr.net");
            sb.append("1) DNS 解析：成功（").append(ms(currentTimeMillis)).append("）\n");
            for (int i = 0; i < allByName.length && i < 4; i++) {
                sb.append("   · ").append(allByName[i].getHostAddress()).append('\n');
            }
            sb.append("2) TCP 连接：\n");
            if (allByName == null) {
                z = false;
            } else {
                z = false;
                for (int i2 = 0; i2 < allByName.length && i2 < 3; i2++) {
                    String hostAddress = allByName[i2].getHostAddress();
                    long currentTimeMillis2 = System.currentTimeMillis();
                    try {
                        Socket socket = new Socket();
                        socket.connect(new InetSocketAddress(hostAddress, 443), 6000);
                        sb.append("   · ").append(hostAddress).append(" → 连接成功（").append(ms(currentTimeMillis2)).append("）\n");
                        z = true;
                        socket.close();
                    } catch (Throwable th2) {
                        sb.append("   · ").append(hostAddress).append(" → 失败（").append(ms(currentTimeMillis2)).append("）").append(shortName(th2)).append('\n');
                    }
                }
            }
            if (z) {
                try {
                    sb.append("2.5) 对比测试 www.baidu.com：解析成功（").append(ms(System.currentTimeMillis())).append("）→ ").append(InetAddress.getByName("www.baidu.com").getHostAddress()).append('\n');
                } catch (Throwable th3) {
                    sb.append("2.5) 对比测试 www.baidu.com：失败 ").append(shortName(th3)).append('\n');
                    sb.append("     （若其他域名也失败 → 是整体网络限制；只有 jsdelivr 失败 → 是该域名被拦）\n");
                }
                long currentTimeMillis3 = System.currentTimeMillis();
                try {
                    HttpURLConnection httpURLConnection = (HttpURLConnection) new URL("https://cdn.jsdelivr.net/gh/NateScarlet/holiday-cn@master/2026.json").openConnection();
                    httpURLConnection.setConnectTimeout(6000);
                    httpURLConnection.setReadTimeout(8000);
                    httpURLConnection.setRequestProperty("User-Agent", "SalaryJump/1.0");
                    int responseCode = httpURLConnection.getResponseCode();
                    if (responseCode == 200) {
                        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(httpURLConnection.getInputStream(), "UTF-8"));
                        int i3 = 0;
                        while (true) {
                            String readLine = bufferedReader.readLine();
                            if (readLine == null) {
                                break;
                            }
                            i3 += readLine.length();
                        }
                        bufferedReader.close();
                        sb.append("3) HTTPS 请求：成功（").append(ms(currentTimeMillis3)).append("）\n");
                        sb.append("   · HTTP 200，读取 ").append(i3).append(" 字符\n");
                        sb.append("\n→ 结论：网络链路完全通畅，更新应该可以成功\n");
                    } else {
                        sb.append("3) HTTPS 请求：HTTP ").append(responseCode).append("（").append(ms(currentTimeMillis3)).append("）\n");
                        sb.append("\n→ 结论：请求被拒绝（状态码 ").append(responseCode).append("）\n");
                    }
                } catch (Throwable th4) {
                    sb.append("3) HTTPS 请求：失败（").append(ms(currentTimeMillis3)).append("）\n");
                    sb.append("   ✗ ").append(shortName(th4)).append('\n');
                    sb.append("\n→ 结论：TCP 通但 HTTPS 失败（可能是 TLS/证书或中间设备干扰）\n");
                }
                return sb.toString();
            }
            sb.append("\n→ 结论：TCP 连接全部失败（网络被阻断，或不只是 DNS 问题）\n");
            return sb.toString();
        } catch (Throwable th5) {
            sb.append("1) DNS 解析：失败（").append(ms(currentTimeMillis)).append("）\n");
            sb.append("   ✗ ").append(shortName(th5)).append('\n');
            sb.append("\n→ 结论：域名解析被阻断（可能是网络限制或 DNS 问题）\n");
            return sb.toString();
        }
    }

    private static String ms(long j) {
        return (System.currentTimeMillis() - j) + "ms";
    }

    private static String shortName(Throwable th) {
        String simpleName = th.getClass().getSimpleName();
        String message = th.getMessage();
        if (message != null && message.length() > 60) {
            message = message.substring(0, 60) + "…";
        }
        return simpleName + (message == null ? "" : ": " + message);
    }
}
