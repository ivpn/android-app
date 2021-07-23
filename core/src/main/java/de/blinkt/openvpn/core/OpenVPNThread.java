/*
 * Copyright (c) 2012-2016 Arne Schwabe
 * Distributed under the GNU GPL v2 with additional terms. For full terms see the file doc/LICENSE.txt
 */

package de.blinkt.openvpn.core;

import android.annotation.SuppressLint;
import android.util.Log;

import net.ivpn.core.IVPNApplication;
import net.ivpn.core.R;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Collections;
import java.util.LinkedList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class OpenVPNThread implements Runnable {

    private static final Logger LOGGER = LoggerFactory.getLogger(OpenVPNThread.class);

    @SuppressLint("SdCardPath")
    private static final String BROKEN_PIE_SUPPORT = "/data/data/net.ivpn.client/cache/pievpn";
    private final static String BROKEN_PIE_SUPPORT2 = "syntax error";
    private static final String TAG = "OpenVPN";
    private String[] mArgv;
    private Process mProcess;
    private String mNativeDir;
    private IOpenVpnService mService;
    private boolean mBrokenPie = false;
    private boolean mNoProcessExitStatus = false;

    public OpenVPNThread(IOpenVpnService service, String[] argv, String nativelibdir) {
        mArgv = argv;
        mNativeDir = nativelibdir;
        mService = service;
    }

    private void stopProcess() {
        mProcess.destroy();
    }

    public void setReplaceConnection() {
        mNoProcessExitStatus = true;
    }

    @Override
    public void run() {
        try {
            Log.i(TAG, "Starting openvpn");
            startOpenVPNThreadArgs(mArgv);
            Log.i(TAG, "OpenVPN process exited");
        } catch (Exception e) {
            LOGGER.error(getString(R.string.unhandled_exception), e.getMessage(), "Starting OpenVPN Thread");
        } finally {
            int exitvalue = 0;
            try {
                if (mProcess != null)
                    exitvalue = mProcess.waitFor();
            } catch (IllegalThreadStateException ite) {
                LOGGER.error("Illegal Thread state: " + ite.getLocalizedMessage());
            } catch (InterruptedException ie) {
                LOGGER.error("InterruptedException: " + ie.getLocalizedMessage());
            }
            if (exitvalue != 0) {
                LOGGER.error("Process exited with exit value " + exitvalue);
                if (mBrokenPie) {
                    /* This will probably fail since the NoPIE binary is probably not written */
                    String[] noPieArgv = VPNLaunchHelper.replacePieWithNoPie(mArgv);

                    // We are already noPIE, nothing to gain
                    if (!noPieArgv.equals(mArgv)) {
                        mArgv = noPieArgv;
                        LOGGER.info("PIE Version could not be executed. Trying no PIE version");
                        run();
                    }
                }
            }

            if (!mNoProcessExitStatus) {
                VpnStatus.updateStateString("NOPROCESS", ConnectionStatus.LEVEL_NOTCONNECTED);
            }
            mService.processDied();
            Log.i(TAG, "Exiting");
        }
    }

    private void startOpenVPNThreadArgs(String[] argv) {
        LinkedList<String> argvlist = new LinkedList<String>();

        Collections.addAll(argvlist, argv);

        ProcessBuilder pb = new ProcessBuilder(argvlist);
        // Hack O rama

        String lbpath = genLibraryPath(argv, pb);

        pb.environment().put("LD_LIBRARY_PATH", lbpath);

        pb.redirectErrorStream(true);
        try {
            mProcess = pb.start();
            // Close the output, since we don't need it
            mProcess.getOutputStream().close();
            InputStream in = mProcess.getInputStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(in));

            while (true) {
                String logline = br.readLine();
                if (logline == null)
                    return;

                if (logline.startsWith(BROKEN_PIE_SUPPORT) || logline.contains(BROKEN_PIE_SUPPORT2))
                    mBrokenPie = true;


                // 1380308330.240114 18000002 Send to HTTP proxy: 'X-Online-Host: bla.blabla.com'

                Pattern p = Pattern.compile("(\\d+).(\\d+) ([0-9a-f])+ (.*)");
                Matcher m = p.matcher(logline);
                int logerror = 0;
                if (m.matches()) {
                    String msg = m.group(4);

                    if ((msg.endsWith("md too weak") && msg.startsWith("OpenSSL: error")) || msg.contains("error:140AB18E"))
                        logerror = 1;

                    LOGGER.info(msg);
                    if (logerror == 1) {
                        LOGGER.error("OpenSSL reproted a certificate with a weak hash, please the in app FAQ about weak hashes");
                    }

                } else {
                    LOGGER.info("P:" + logline);
                }

                if (Thread.interrupted()) {
                    throw new InterruptedException("OpenVpn process was killed form java code");
                }
            }
        } catch (InterruptedException | IOException e) {
            LOGGER.error(getString(R.string.unhandled_exception), e.getMessage(), "Error reading from output of OpenVPN process");
            stopProcess();
        }
    }

    private String genLibraryPath(String[] argv, ProcessBuilder pb) {
        // Hack until I find a good way to get the real library path
        String applibpath = argv[0].replaceFirst("/cache/.*$", "/lib");

        String lbpath = pb.environment().get("LD_LIBRARY_PATH");
        if (lbpath == null)
            lbpath = applibpath;
        else
            lbpath = applibpath + ":" + lbpath;

        if (!applibpath.equals(mNativeDir)) {
            lbpath = mNativeDir + ":" + lbpath;
        }
        return lbpath;
    }

    private static String getString(int resId) {
        return IVPNApplication.application.getString(resId);
    }
}