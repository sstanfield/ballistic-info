package transapps.ballistic.fragment;

import transapps.ballistic.R;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;

public class HelpFragment extends BaseFragment {
	private String url = "file:///android_asset/BallisticsInfo_Help/ballisticsinfoHome.html";
	private WebView webView;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.help_fragment, container, false);
		getActivity().setTitle("Help");
		webView = (WebView)v.findViewById(R.id.help_html);
		webView.loadUrl(url);
		return v;
	}

	@Override
	public void onPause() {
		url = webView==null?url:webView.getUrl();
		super.onPause();
	}
}
