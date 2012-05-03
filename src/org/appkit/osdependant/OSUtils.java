package org.appkit.osdependant;

import com.google.common.collect.Maps;

import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.PointerType;
import com.sun.jna.win32.W32APIFunctionMapper;
import com.sun.jna.win32.W32APITypeMapper;

import java.io.File;

import java.util.Map;

import org.eclipse.swt.SWT;

/**
 * provides OS-specific utilities
 */
public class OSUtils {

	//~ Methods --------------------------------------------------------------------------------------------------------

	/**
	 * check if application is running on windows
	 */
	public static boolean isWindows() {
		if (SWT.getPlatform().equalsIgnoreCase("win32") || SWT.getPlatform().equalsIgnoreCase("win64")) {
			return true;
		}

		return false;
	}

	/**
	 * check if application is running on Mac OSX / cocoa.
	 */
	public static boolean isMac() {
		if (SWT.getPlatform().equalsIgnoreCase("cocoa") || SWT.getPlatform().equalsIgnoreCase("carbon")) {
			return true;
		}

		return false;
	}

	/**
	 * Returns the correct userDataFolder for the given application name.
	 */
	public static String userDataFolder(final String applicationName) {

		// default
		String folder = "." + File.separator;

		if (isMac()) {
			folder = System.getProperty("user.home") + File.separator + "Library" + File.separator
					 + "Application Support";
		} else if (isWindows()) {

			Map<String, Object> options = Maps.newHashMap();
			options.put(Library.OPTION_TYPE_MAPPER, W32APITypeMapper.UNICODE);
			options.put(Library.OPTION_FUNCTION_MAPPER, W32APIFunctionMapper.UNICODE);

			HWND hwndOwner   = null;
			int nFolder		 = Shell32.CSIDL_LOCAL_APPDATA;
			HANDLE hToken    = null;
			int dwFlags		 = Shell32.SHGFP_TYPE_CURRENT;
			char pszPath[]   = new char[Shell32.MAX_PATH];
			Shell32 instance = (Shell32) Native.loadLibrary("shell32", Shell32.class, options);
			int hResult		 = instance.SHGetFolderPath(hwndOwner, nFolder, hToken, dwFlags, pszPath);
			if (Shell32.S_OK == hResult) {

				String path = new String(pszPath);
				int len     = path.indexOf('\0');
				folder	    = path.substring(0, len);
			} else {
				System.err.println("Error: " + hResult);
			}
		}

		folder = folder + File.separator + applicationName + File.separator;

		return folder;
	}

	//~ Inner Interfaces -----------------------------------------------------------------------------------------------

	private static interface Shell32 extends Library {

		public static final int MAX_PATH									  = 260;
		public static final int CSIDL_LOCAL_APPDATA							  = 0x001c;
		public static final int SHGFP_TYPE_CURRENT							  = 0;
		@SuppressWarnings("unused")
		public static final int SHGFP_TYPE_DEFAULT							  = 1;
		public static final int S_OK										  = 0;

		/**
		 * see http://msdn.microsoft.com/en-us/library/bb762181(VS.85).aspx
		 *
		 * HRESULT SHGetFolderPath( HWND hwndOwner, int nFolder, HANDLE hToken,
		 * DWORD dwFlags, LPTSTR pszPath);
		 */
		public int SHGetFolderPath(final HWND hwndOwner, final int nFolder, final HANDLE hToken, final int dwFlags,
								   final char pszPath[]);
	}

	//~ Inner Classes --------------------------------------------------------------------------------------------------

	private static class HANDLE extends PointerType {}

	private static class HWND extends HANDLE {}
}