package freesoftoriented.pro6.readpro6;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.xml.bind.JAXB;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlValue;

import org.apache.commons.codec.binary.Base64;
import org.springframework.stereotype.Service;

/**
 * ProPresenter6 スライドファイルのデータ
 *
 */
@Service
public class ProPresentor6Data {

	public void handleCommand(String filepath) {
		System.out.println("\nRead:" + filepath);
		// XML File→JavaObject
		System.out.println("\nConvert:");
		RVPresentationDocument data = readFromFile(filepath);
		// Edit
		System.out.println(String.format("\nSlide size: %s x %s", data.getWidth(), data.getHeight()));
		List<RVSlideGrouping> slideGroup = data.findSlideGroup();
		for (RVSlideGrouping group : slideGroup) {
			List<RVDisplaySlide> slides = group.findDisplaySlide();
			System.out.println(String.format("Slide Group (%s slides)", slides.size()));
			for (RVDisplaySlide slide : slides) {
				List<RVTextElement> elements = slide.findTextElement();
				System.out.println(String.format("\n- Slide (%s text area)", elements.size()));
				int[] sizearray = { 116, 100, 100 };
				if (elements.size() == 1) {
					sizearray[0] = 138;
				}
				for (int i = 0; i < elements.size(); i++) {
					RVTextElement element = elements.get(i);
					handleTextElement(element, sizearray[i]);
				}
			}
		}

		// JavaObject→XML
		System.out.println("\n\nBack to XML:");
		writeToFile(filepath + "_out.pro6", data);
	}

	private void handleTextElement(RVTextElement te, int size) {
		List<Integer> position = te.extractPosition();
		int ul_x = position.get(0);
		int ul_y = position.get(1);
		int lr_x = position.get(3);
		int lr_y = position.get(4);
		System.out.println(String.format("  - Text (%d,%d) w%d h%d", ul_x, ul_y, lr_x - ul_x, lr_y - ul_y));
		String rawRTFData = te.findRawRTFData();
		RTFEditor rtf = new RTFEditor(rawRTFData);
		rtf.setFontSize(size);
		te.replaceRawRTFData(rtf.getRtfText());
		System.out.println(rtf.getRtfText());
	}

	/**
	 * RTF部分を編集する
	 *
	 */
	@lombok.ToString(exclude = { "rtfText" })
	public static class RTFEditor {

		@lombok.Getter
		private String rtfText;

		public RTFEditor(String rtftext) {
			this.rtfText = rtftext;
		}

		public void setFontSize(int size) {
			// フォントサイズはファイル内部では2倍になっている
			String[] segments = rtfText.split("\\\\fs");
			if (segments == null || segments.length == 0) {
				System.out.println("[INFO] nothing to do");
				return;
			}
			// \fsコマンドで区切って、サイズ部分を上書き
			List<String> list = new ArrayList<>();
			list.add(segments[0]);
			Pattern pattern = Pattern.compile("([0-9]+)(.*)", Pattern.DOTALL);
			for (int i = 1; i < segments.length; i++) {
				Matcher matcher = pattern.matcher(segments[i]);
				if (!matcher.matches()) {
					System.out.println("[ERROR] Cannot edit");
					return;
				}
				list.add(String.format("%d%s", size * 2, matcher.group(2)));
			}
			rtfText = String.join("\\fs", list);
		}

		public void updateColorTable() {
			// カラーテーブルを更新する
		}
	}

	/**
	 * Pro6ファイルを読み込む(可能な限りパース)
	 * 
	 * @param filepath Pro6ファイルのパス
	 * @return Javaオブジェクトへマップしたもの
	 */
	public static RVPresentationDocument readFromFile(String filepath) {
		return JAXB.unmarshal(new File(filepath), RVPresentationDocument.class);
	}

	/**
	 * Pro6ファイルを書き込む
	 * 
	 * @param filepath
	 * @param doc
	 */
	public static void writeToFile(String filepath, RVPresentationDocument doc) {
		JAXB.marshal(doc, new File(filepath));
	}

	/**
	 * Pro6 ルートクラス.
	 *
	 */
	@lombok.ToString
	@lombok.Getter
	public static class RVPresentationDocument {

		@XmlAttribute(name = "CCLIArtistCredits")
		private String ccliArtistCredits;
		@XmlAttribute(name = "CCLIAuthor")
		private String ccliAuthor;
		@XmlAttribute(name = "CCLICopyrightYear")
		private String ccliCopyrightYear;
		@XmlAttribute(name = "CCLIDisplay")
		private String ccliDisplay;
		@XmlAttribute(name = "CCLIPublisher")
		private String ccliPublisher;
		@XmlAttribute(name = "CCLISongNumber")
		private String ccliSongNumber;
		@XmlAttribute(name = "CCLISongTitle")
		private String ccliSongTitle;

		@XmlAttribute(name = "backgroundColor")
		private String backgroundColor;
		@XmlAttribute(name = "buildNumber")
		private String buildNumber;
		@XmlAttribute(name = "category")
		private String category;
		@XmlAttribute(name = "chordChartPath")
		private String chordChartPath;
		@XmlAttribute(name = "docType")
		private String docType;
		@XmlAttribute(name = "drawingBackgroundColor")
		private String drawingBackgroundColor;
		@XmlAttribute(name = "height")
		private String height;
		@XmlAttribute(name = "lastDateUsed")
		private String lastDateUsed;
		@XmlAttribute(name = "notes")
		private String notes;
		@XmlAttribute(name = "os")
		private String os;
		@XmlAttribute(name = "resourcesDirectory")
		private String resourcesDirectory;
		@XmlAttribute(name = "selectedArrangementID")
		private String selectedArrangementID;
		@XmlAttribute(name = "usedCount")
		private String usedCount;
		@XmlAttribute(name = "uuid")
		private String uuid;
		@XmlAttribute(name = "versionNumber")
		private String versionNumber;
		@XmlAttribute(name = "width")
		private String width;

		@XmlElement(name = "RVTimeline")
		private RVTimeline rvTimeline;
		@XmlElement(name = "RVBibleReference")
		private RVBibleReference rvBibleReference;

		@XmlElement(name = "array")
		private List<P6ArrayContainer> array;

		/**
		 * スライドグループを返す
		 * 
		 * @return
		 */
		public List<RVSlideGrouping> findSlideGroup() {
			// arrayの中に、変数名がgroupsの要素があれば、それがRVSlideGroupを持つ
			if (array == null) {
				return null;
			}
			for (P6ArrayContainer item : array) {
				if ("groups".equals(item.getRvXMLIvarName())) {
					return item.getRvSlideGrouping();
				}
			}
			return null;
		}

	}

	/**
	 * Pro6 特定目的クラス. タイムライン？.
	 *
	 */
	@lombok.ToString
	@lombok.Getter
	public static class RVTimeline {

		@XmlAttribute(name = "duration")
		private String duration;
		@XmlAttribute(name = "loop")
		private String loop;
		@XmlAttribute(name = "playBackRate")
		private String playBackRate;
		@XmlAttribute(name = "rvXMLIvarName")
		private String rvXMLIvarName;
		@XmlAttribute(name = "selectedMediaTrackIndex")
		private String selectedMediaTrackIndex;
		@XmlAttribute(name = "timeOffset")
		private String timeOffset;

		@XmlElement(name = "array")
		private List<P6ArrayContainer> array;

	}

	/**
	 * Pro6 特定目的クラス. みことば？.
	 *
	 */
	@lombok.ToString
	@lombok.Getter
	public static class RVBibleReference {

		@XmlAttribute(name = "bookIndex")
		private String bookIndex;
		@XmlAttribute(name = "bookName")
		private String bookName;
		@XmlAttribute(name = "chapterEnd")
		private String chapterEnd;
		@XmlAttribute(name = "chapterStart")
		private String chapterStart;
		@XmlAttribute(name = "rvXMLIvarName")
		private String rvXMLIvarName;
		@XmlAttribute(name = "translationAbbreviation")
		private String translationAbbreviation;
		@XmlAttribute(name = "translationName")
		private String translationName;
		@XmlAttribute(name = "verseEnd")
		private String verseEnd;
		@XmlAttribute(name = "verseStart")
		private String verseStart;

	}

	/**
	 * Pro6 特定目的クラス. スライドグループ.
	 *
	 */
	@lombok.ToString
	@lombok.Getter
	public static class RVSlideGrouping {

		@XmlAttribute(name = "color")
		private String color;
		@XmlAttribute(name = "name")
		private String name;
		@XmlAttribute(name = "uuid")
		private String uuid;

		@XmlElement(name = "array")
		private List<P6ArrayContainer> array;

		/**
		 * このグループに含まれるスライドを返す
		 * 
		 * @return
		 */
		public List<RVDisplaySlide> findDisplaySlide() {
			if (array == null) {
				return null;
			}
			for (P6ArrayContainer item : array) {
				if ("slides".equals(item.getRvXMLIvarName())) {
					return item.getRvDisplaySlide();
				}
			}
			return null;
		}
	}

	/**
	 * Pro6 特定目的クラス. スライド.
	 *
	 */
	@lombok.ToString
	@lombok.Getter
	public static class RVDisplaySlide {

		@XmlAttribute(name = "UUID")
		private String uuid;
		@XmlAttribute(name = "backgroundColor")
		private String backgroundColor;
		@XmlAttribute(name = "chordChartPath")
		private String chordChartPath;
		@XmlAttribute(name = "drawingBackgroundColor")
		private String drawingBackgroundColor;
		@XmlAttribute(name = "enabled")
		private String enabled;
		@XmlAttribute(name = "highlightColor")
		private String highlightColor;
		@XmlAttribute(name = "hotKey")
		private String hotKey;
		@XmlAttribute(name = "label")
		private String label;
		@XmlAttribute(name = "notes")
		private String notes;
		@XmlAttribute(name = "socialItemCount")
		private String socialItemCount;

		@XmlElement(name = "array")
		private List<P6ArrayContainer> array;

		/**
		 * このスライドが持つテキストエリアを返す
		 * 
		 * @return
		 */
		public List<RVTextElement> findTextElement() {
			if (array == null) {
				return null;
			}
			for (P6ArrayContainer item : array) {
				if ("displayElements".equals(item.getRvXMLIvarName())) {
					return item.getRvTextElement();
				}
			}
			return null;
		}

	}

	/**
	 * Pro6 特定目的クラス. テキストエリア.
	 *
	 */
	@lombok.ToString
	@lombok.Getter
	public static class RVTextElement {

		@XmlAttribute(name = "UUID")
		private String uuid;
		@XmlAttribute(name = "additionalLineFillHeight")
		private String additionalLineFillHeight;
		@XmlAttribute(name = "adjustsHeightToFit")
		private String adjustsHeightToFit;
		@XmlAttribute(name = "bezelRadius")
		private String bezelRadius;
		@XmlAttribute(name = "displayDelay")
		private String displayDelay;
		@XmlAttribute(name = "displayName")
		private String displayName;
		@XmlAttribute(name = "drawLineBackground")
		private String drawLineBackground;
		@XmlAttribute(name = "drawingFill")
		private String drawingFill;
		@XmlAttribute(name = "drawingShadow")
		private String drawingShadow;
		@XmlAttribute(name = "drawingStroke")
		private String drawingStroke;
		@XmlAttribute(name = "fillColor")
		private String fillColor;
		@XmlAttribute(name = "fromTemplate")
		private String fromTemplate;
		@XmlAttribute(name = "lineBackgroundType")
		private String lineBackgroundType;
		@XmlAttribute(name = "lineFillVerticalOffset")
		private String lineFillVerticalOffset;
		@XmlAttribute(name = "locked")
		private String locked;
		@XmlAttribute(name = "opacity")
		private String opacity;
		@XmlAttribute(name = "persistent")
		private String persistent;
		@XmlAttribute(name = "revealType")
		private String revealType;
		@XmlAttribute(name = "rotation")
		private String rotation;
		@XmlAttribute(name = "source")
		private String source;
		@XmlAttribute(name = "textSourceRemoveLineReturnsOption")
		private String textSourceRemoveLineReturnsOption;
		@XmlAttribute(name = "typeID")
		private String typeID;
		@XmlAttribute(name = "useAllCaps")
		private String useAllCaps;
		@XmlAttribute(name = "verticalAlignment")
		private String verticalAlignment;

		@XmlElement(name = "RVRect3D")
		private P6KeyValue rvRect3D;
		@XmlElement(name = "shadow")
		private P6KeyValue shadow;
		@XmlElement(name = "dictionary")
		private P6Dictionary dictionary;
		@XmlElement(name = "NSString")
		private P6KeyValue nsString;

		/**
		 * このテキストエリアの矩形座標を返す。<br/>
		 * 左上x, y, z、右下x, y<br/>
		 * 
		 * <pre>
		 * {7 7 0 2202 672}
		 * (7,7,0)(2202,672)
		 * </pre>
		 * 
		 * @return
		 */
		public List<Integer> extractPosition() {
			if (rvRect3D == null) {
				return null;
			}
			String value = rvRect3D.getValue();
			// ex. {7 7 0 2202 672}
			String[] nums = value.replace("{", "").replace("}", "").split(" ");
			List<Integer> position = Arrays.asList(nums).stream().map(num -> {
				return Integer.parseInt(num);
			}).collect(Collectors.toList());
			return position;
		}

		/**
		 * このテキストエリアのRTF(Rich Text Format)データ。
		 * 
		 * @return
		 */
		public String findRawRTFData() {
			String base64rtf = nsString.getValue();
			byte[] decoded = Base64.decodeBase64(base64rtf);
			return new String(decoded);
		}

		/**
		 * このテキストエリアのRTFを、指定のもので置き換える
		 * 
		 * @param rtfData
		 */
		public void replaceRawRTFData(String rtfData) {
			byte[] encoded = Base64.encodeBase64(rtfData.getBytes());
			nsString = new P6KeyValue(nsString.getRvXMLIvarName(), new String(encoded));
		}

	}

	/**
	 * Pro6 特定目的クラス. アレンジ？.
	 *
	 */
	@lombok.ToString
	@lombok.Getter
	public static class RVSongArrangement {

		@XmlAttribute(name = "color")
		private String color;
		@XmlAttribute(name = "name")
		private String name;
		@XmlAttribute(name = "uuid")
		private String uuid;

		@XmlElement(name = "array")
		private List<P6ArrayContainer> array;

	}

	/**
	 * Pro6 共通クラス. 配列.
	 *
	 */
	@lombok.ToString
	@lombok.Getter
	public static class P6ArrayContainer {

		@XmlAttribute(name = "rvXMLIvarName")
		private String rvXMLIvarName;

		@XmlElement(name = "RVSlideGrouping")
		private List<RVSlideGrouping> rvSlideGrouping;
		@XmlElement(name = "RVDisplaySlide")
		private List<RVDisplaySlide> rvDisplaySlide;
		@XmlElement(name = "RVTextElement")
		private List<RVTextElement> rvTextElement;
		@XmlElement(name = "NSString")
		private List<P6KeyValue> nsString;
		@XmlElement(name = "RVSongArrangement")
		private List<RVSongArrangement> rvSongArrangement;

	}

	/**
	 * Pro6 共通クラス. 値(RVRect3D, shadow, NSString).
	 *
	 */
	@lombok.ToString
	@lombok.Getter
	@lombok.NoArgsConstructor
	@lombok.AllArgsConstructor
	public static class P6KeyValue {

		@XmlAttribute(name = "rvXMLIvarName")
		private String rvXMLIvarName;
		@XmlValue
		private String value;

	}

	/**
	 * Pro6 共通クラス. 辞書.
	 *
	 */
	@lombok.ToString
	@lombok.Getter
	public static class P6Dictionary {

		@XmlAttribute(name = "rvXMLIvarName")
		private String rvXMLIvarName;

		@XmlElement(name = "NSColor")
		private P6DictionaryItem nsColor;
		@XmlElement(name = "NSNumber")
		private P6DictionaryItem nsNumber;
		@XmlElement(name = "NSString")
		private P6DictionaryItem nsString;

	}

	/**
	 * Pro6 共通クラス. 辞書の要素.
	 *
	 */
	@lombok.ToString
	@lombok.Getter
	public static class P6DictionaryItem {

		@XmlAttribute(name = "hint")
		private String hint;
		@XmlAttribute(name = "rvXMLDictionaryKey")
		private String rvXMLDictionaryKey;
		@XmlValue
		private String value;

	}

}
