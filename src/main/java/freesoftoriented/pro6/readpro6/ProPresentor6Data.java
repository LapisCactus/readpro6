package freesoftoriented.pro6.readpro6;

import java.io.File;
import java.util.List;

import javax.xml.bind.JAXB;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlValue;

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
		System.out.println(data);
		// Edit
		System.out.println(String.format("Slide size: %s x %s", data.getWidth(), data.getHeight()));
		List<RVSlideGrouping> slideGroup = data.findSlideGroup();
		for (RVSlideGrouping group : slideGroup) {
			List<RVDisplaySlide> slides = group.findDisplaySlide();
			System.out.println(String.format("Slide Group (%s slides)", slides.size()));
			for (RVDisplaySlide slide : slides) {
				List<RVTextElement> elements = slide.findTextElement();
				System.out.println(String.format("- Slide (%s text area)", elements.size()));
			}
		}

		// JavaObject→XML
		System.out.println("\n\nBack to XML:");
		JAXB.marshal(data, System.out);
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
