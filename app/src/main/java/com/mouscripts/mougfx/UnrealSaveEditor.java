package com.mouscripts.mougfx;

import android.content.pm.PackageManager;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;
import java.nio.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;

import rikka.shizuku.Shizuku;

public class UnrealSaveEditor {
    private static final String TAG = "UnrealEditor";
    private static final char[] HEX_ARRAY = "0123456789ABCDEF".toCharArray();
    // كلاس داخلي لتخزين بيانات كل خاصية ومكانها في الملف
    public static class UEProperty {
        public String name;
        public String type;
        public byte[] rawData;
        public Object value;
        public int offset; // هذا هو المتغير الذي يفتقده الكود

        public UEProperty(String name, String type, byte[] rawData, Object value, int offset) {
            this.name = name;
            this.type = type;
            this.rawData = rawData;
            this.value = value;
            this.offset = offset;
        }
    }

    private Map<String, UEProperty> properties = new LinkedHashMap<>();
    private byte[] fullFileData; // مصفوفة لتخزين الملف بالكامل

    private String readUEString(ByteBuffer bb) {
        if (bb.remaining() < 4) return null;
        int len = bb.getInt();

        // 1. حماية ضد الأرقام الضخمة أو السالبة غير المنطقية
        if (len == 0) return "";

        // إذا كان الطول يبدو غير منطقي (أكبر من 100 حرف أو رقم سالب ضخم جداً)
        if (Math.abs(len) > 1000) return null;

        try {
            if (len < 0) { // UTF-16
                int actualLen = -len * 2;
                if (actualLen > bb.remaining() || actualLen < 0) return null;
                byte[] strData = new byte[actualLen];
                bb.get(strData);
                return new String(strData, StandardCharsets.UTF_16LE).replace("\0", "");
            } else { // UTF-8
                if (len > bb.remaining() || len <= 0) return null;
                byte[] strData = new byte[len];
                bb.get(strData);
                return new String(strData, 0, len - 1, StandardCharsets.UTF_8);
            }
        } catch (Exception e) {
            return null; // في حال حدوث أي خطأ أثناء إنشاء المصفوفة
        }
    }

    public void load(String path) throws IOException {
        this.fullFileData = null; // إعادة تعيين البيانات قبل البدء

        // 1. المحاولة الأولى: القراءة التقليدية (مهمة جداً لأندرويد 7)
        try {
            File file = new File(path);
            if (file.exists() && file.canRead()) {
                int size = (int) file.length();
                if (size > 0) {
                    this.fullFileData = new byte[size];
                    try (FileInputStream fis = new FileInputStream(file)) {
                        fis.read(this.fullFileData);
                    }
                    Log.d("SAV_EDITOR", "تمت القراءة التقليدية بنجاح.");
                }
            }
        } catch (Exception e) {
            Log.d("SAV_EDITOR", "القراءة التقليدية فشلت: " + e.getMessage());
        }

        // 2. المحاولة الثانية: Shizuku (إذا فشلت الأولى وكان الإصدار حديثاً)
        if (this.fullFileData == null) {
            Log.d("SAV_EDITOR", "محاولة القراءة عبر Shizuku...");
            this.fullFileData = readFileViaShizuku(path);
        }

        // 3. التحقق النهائي (هذا السطر يمنع الـ Crash)
        if (this.fullFileData == null || this.fullFileData.length == 0) {
            throw new IOException("تعذر الوصول للملف (لا يوجد Root أو Shizuku غير مفعل)");
        }

        // 4. تحليل البيانات (الآن آمن لأننا تأكدنا أن fullFileData ليس null)
        ByteBuffer bb = ByteBuffer.wrap(this.fullFileData).order(ByteOrder.LITTLE_ENDIAN);
        properties.clear();

        int limit = fullFileData.length - 24;
        for (int i = 0; i < limit; i++) {
            bb.position(i);
            String foundType = readUEString(bb);

            if (foundType != null && foundType.endsWith("Property")) {
                int typeEndPos = bb.position();

                for (int j = i - 4; j > i - 64 && j >= 0; j--) {
                    String possibleName = readUEStringAt(fullFileData, j);
                    if (possibleName != null && !possibleName.isEmpty() && (j + 4 + (possibleName.length() + 1) == i)) {
                        bb.position(typeEndPos);
                        try {
                            int size = bb.getInt();
                            int index = bb.getInt();
                            byte boolVal = bb.get();
                            int valueOffset = bb.position();

                            if (size >= 0 && bb.remaining() >= size) {
                                byte[] data = new byte[size];
                                bb.get(data);
                                Object value = parseValue(foundType, data, boolVal);

                                properties.put(possibleName, new UEProperty(possibleName, foundType, data, value, valueOffset));
                                Log.d("SAV_SCAN", "تم العثور: " + possibleName + " [" + foundType + "] = " + value);
                            }
                        } catch (Exception e) {
                            Log.e("SAV_SCAN", "خطأ في قراءة داتا: " + possibleName);
                        }
                        break;
                    }
                }
            }
        }
    }
    // دالة مساعدة للقراءة من مكان محدد دون تحريك مؤشر bb الأصلي
    private String readUEStringAt(byte[] data, int pos) {
        ByteBuffer tempBb = ByteBuffer.wrap(data).order(ByteOrder.LITTLE_ENDIAN);
        tempBb.position(pos);
        return readUEString(tempBb);
    }
    // دالة قراءة قيمة معينة
    public Object readValue(String key) {
        if (properties.containsKey(key)) {
            return Objects.requireNonNull(properties.get(key)).value;
        }
        return null;
    }

    // دالة تحديث القيمة (تعديل مباشر في مصفوفة البايتات)
    public void updateValue(String key, Object newValue) {
        UEProperty prop = properties.get(key);
        if (prop == null) {
            Log.e("SAV_EDITOR", "الخاصية غير موجودة: " + key);
            return;
        }

        try {
            if (prop.type.equals("BoolProperty")) {
                // معالجة القيمة المنطقية القادمة من JS (قد تأتي كـ Boolean أو Number)
                boolean boolValue;
                if (newValue instanceof Boolean) {
                    boolValue = (Boolean) newValue;
                } else if (newValue instanceof Number) {
                    boolValue = ((Number) newValue).intValue() != 0;
                } else {
                    boolValue = Boolean.parseBoolean(String.valueOf(newValue));
                }

                // في Unreal، قيمة الـ Boolean تكون في البايت الذي يسبق الـ offset مباشرة
                fullFileData[prop.offset - 1] = (byte) (boolValue ? 1 : 0);
                prop.value = boolValue;
                Log.d("SAV_EDITOR", "تم تحديث " + key + " (Bool) إلى: " + boolValue);


            } else {
                // تحويل الأنواع الأخرى (Int, Float, String) إلى بايتات
                byte[] newData = valueToBytes(prop.type, newValue);

                if (newData != null) {
                    // التأكد من أن التعديل لا يخرج عن حدود مصفوفة الملف
                    if (prop.offset + newData.length <= fullFileData.length) {
                        System.arraycopy(newData, 0, fullFileData, prop.offset, newData.length);
                        prop.value = newValue;
                        Log.d("SAV_EDITOR", "تم تحديث " + key + " (" + prop.type + ") بنجاح.");
                    } else {
                        Log.e("SAV_EDITOR", "خطأ: حجم البيانات الجديدة يتجاوز المساحة المخصصة لـ " + key);
                    }
                }
            }
        } catch (Exception e) {
            Log.e("SAV_EDITOR", "فشل تحديث القيمة لـ " + key + ": " + e.getMessage());
        }
    }
    private byte[] valueToBytes(String type, Object value) {
        try {
            ByteBuffer bb;
            Number numValue = (value instanceof Number) ? (Number) value : null;

            switch (type) {
                case "IntProperty":
                    if (numValue == null) return null;
                    bb = ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN);
                    bb.putInt(numValue.intValue());
                    return bb.array();

                case "FloatProperty":
                    if (numValue == null) return null;
                    bb = ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN);
                    bb.putFloat(numValue.floatValue());
                    return bb.array();

                case "StrProperty":
                case "NameProperty":
                    String str = String.valueOf(value);
                    byte[] strBytes = str.getBytes(java.nio.charset.StandardCharsets.UTF_8);
                    bb = ByteBuffer.allocate(4 + strBytes.length + 1).order(ByteOrder.LITTLE_ENDIAN);
                    bb.putInt(strBytes.length + 1);
                    bb.put(strBytes);
                    bb.put((byte) 0); // Null Terminator
                    return bb.array();

                default:
                    return null;
            }
        } catch (Exception e) {
            return null;
        }
    }
    public String getPropertiesAsJson() {
        JSONObject jsonObject = new JSONObject();

        try {
            for (Map.Entry<String, UEProperty> entry : properties.entrySet()) {
                String key = entry.getKey();
                Object value = entry.getValue().value; // القيمة المستخرجة (Integer, String, etc.)

                // إضافة القيمة للـ JSON (نتأكد أنها ليست null)
                if (value != null) {
                    jsonObject.put(key, value);
                }
            }
        } catch (JSONException e) {
            Log.e("SAV_JSON", "خطأ في تحويل البيانات لـ JSON: " + e.getMessage());
            return "{}";
        }

        return jsonObject.toString(); // يعيد النص النهائي كـ String
    }

    // دالة الحفظ المحدثة - تقبل مخرج بيانات عام
    public void save(OutputStream outputStream) throws IOException {
        if (fullFileData == null || fullFileData.length == 0) {
            throw new IOException("لا توجد بيانات في الذاكرة لحفظها! تأكد من تحميل الملف أولاً.");
        }

        try {
            Log.d("SAV_EDITOR", "جاري الحفظ... حجم المصفوفة: " + fullFileData.length + " بايت");

//            Log.d(TAG, "save: getPropertiesAsJson() => " + getPropertiesAsJson());

            // كتابة المصفوفة كاملة إلى المخرج
            outputStream.write(fullFileData);

            // التأكد من تفريغ البيانات من الذاكرة الوسيطة إلى الملف
            outputStream.flush();

            Log.d("SAV_EDITOR", "تمت عملية الكتابة بنجاح إلى المستهدف.");
        } catch (Exception e) {
            Log.e("SAV_EDITOR", "خطأ أثناء الكتابة: " + e.getMessage());
            throw new IOException("فشل في كتابة البيانات: " + e.getMessage());
        } finally {
            if (outputStream != null) {
                outputStream.close();
                Log.d("SAV_EDITOR", "تم إغلاق الـ Stream بنجاح.");
            }
        }
    }

    // دوال مساعدة للتحليل
    private Object parseValue(String type, byte[] data, byte boolByte) {
        ByteBuffer bb = ByteBuffer.wrap(data).order(ByteOrder.LITTLE_ENDIAN);
        switch (type) {
            case "IntProperty": return data.length >= 4 ? bb.getInt() : 0;
            case "FloatProperty": return data.length >= 4 ? bb.getFloat() : 0.0f;
            case "BoolProperty": return boolByte != 0;
            case "StrProperty":
                ByteBuffer strBb = ByteBuffer.wrap(data).order(ByteOrder.LITTLE_ENDIAN);
                return readUEString(strBb);
            default: return data;
        }
    }


    private int findStringOffset(byte[] data, String target) {
        byte[] targetBytes = target.getBytes(StandardCharsets.UTF_8);
        for (int i = 0; i < data.length - targetBytes.length; i++) {
            boolean match = true;
            for (int j = 0; j < targetBytes.length; j++) {
                if (data[i + j] != targetBytes[j]) { match = false; break; }
            }
            if (match) return i - 4; // العودة لمكان طول النص
        }
        return -1;
    }

    public UEProperty getProperty(String key) {
        return properties.get(key);
    }

    public Set<String> getPropertiesKeys() {
        return properties.keySet();
    }

    public String getAllPropertiesJson() {
        JSONObject jsonObject = new JSONObject();
        try {
            for (Map.Entry<String, UEProperty> entry : properties.entrySet()) {
                JSONObject propObj = new JSONObject();
                propObj.put("type", entry.getValue().type);
                Object val = entry.getValue().value;
                if (val instanceof byte[]) {
                    propObj.put("value", bytesToHex((byte[]) val));
                    propObj.put("raw", true);
                } else {
                    propObj.put("value", val);
                }
                jsonObject.put(entry.getKey(), propObj);
            }
        } catch (JSONException e) {
            return "{}";
        }
        return jsonObject.toString();
    }

    boolean checkFileAccess(String path) {
        File file = new File(path);

        Log.d("FILE_CHECK", "المسار: " + path);

        if (!file.exists()) {
            Log.e("FILE_CHECK", "الخطأ: الملف غير موجود نهائياً في هذا المسار.");
            return false;
        }

        if (!file.canRead()) {
            Log.e("FILE_CHECK", "الخطأ: الملف موجود، لكن التطبيق لا يملك صلاحية قراءته (Permission Denied).");
            return false;
        }

        Log.d("FILE_CHECK", "نجاح: تم العثور على الملف ويمكن قراءته. الحجم: " + file.length() + " بايت.");
        return true;
    }


    public byte[] readFileViaShizuku(String fullPath) {
        // 1. التأكد من أن الخدمة تعمل
        if (!Shizuku.pingBinder()) {
            Log.e("SHIZUKU", "خدمة Shizuku لا تعمل أو الـ Binder لم يُستلم بعد.");
            return null;
        }

        // 2. التحقق من الصلاحية
        if (Shizuku.checkSelfPermission() != PackageManager.PERMISSION_GRANTED) {
            Log.e("SHIZUKU", "الصلاحية غير ممنوحة.");
            // يمكنك استدعاء Shizuku.requestPermission(101) هنا
            return null;
        }

        try {
            // Shizuku.newProcess يعيد Process عادي من جافا
            java.lang.Process process = Shizuku.newProcess(new String[]{"cat", fullPath}, null, null);

            InputStream is = process.getInputStream();
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();

            byte[] temp = new byte[8192]; // بفر أكبر لسرعة القراءة
            int nRead;
            while ((nRead = is.read(temp, 0, temp.length)) != -1) {
                buffer.write(temp, 0, nRead);
            }

            // انتظر انتهاء العملية وتأكد من نجاحها
            int exitCode = process.waitFor();
            if (exitCode != 0) {
                Log.e("SHIZUKU", "فشل الأمر cat، كود الخروج: " + exitCode);
                return null;
            }

            return buffer.toByteArray();
        } catch (Exception e) {
            Log.e("SHIZUKU", "خطأ أثناء القراءة عبر Shizuku: " + e.getMessage());
            return null;
        }
    }

    public boolean saveFileViaShizuku(String path, byte[] data) {
        if (!Shizuku.pingBinder() || Shizuku.checkSelfPermission() != PackageManager.PERMISSION_GRANTED) {
            Log.e("SHIZUKU", "Shizuku غير متاح أو الصلاحية غير ممنوحة");
            return false;
        }
        try {
            String hex = bytesToHex(data);
            Process process = Shizuku.newProcess(new String[]{"sh", "-c",
                "echo '" + hex + "' | xxd -r -p > \"" + path + "\" && chmod 644 \"" + path + "\""}, null, null);
            int exitCode = process.waitFor();
            if (exitCode != 0) {
                Log.e("SHIZUKU", "فشل الحفظ عبر Shizuku، كود: " + exitCode);
                return false;
            }
            Log.d("SHIZUKU", "تم الحفظ عبر Shizuku بنجاح");
            return true;
        } catch (Exception e) {
            Log.e("SHIZUKU", "خطأ في الحفظ عبر Shizuku: " + e.getMessage());
            return false;
        }
    }

    public static String bytesToHex(byte[] bytes) {
        if (bytes == null) return "";
        char[] hexChars = new char[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF; // تحويل البايت لـ unsigned
            hexChars[j * 2] = HEX_ARRAY[v >>> 4]; // استخراج الجزء العلوي (High nibble)
            hexChars[j * 2 + 1] = HEX_ARRAY[v & 0x0F]; // استخراج الجزء السفلي (Low nibble)
        }
        return new String(hexChars);
    }
    public byte[] getFullFileData() {
        return fullFileData;
    }
    public void setFullFileData(byte[] data) {
        this.fullFileData = data;
    }

    // انقل منطق الـ Scanner من دالة load القديمة إلى دالة جديدة تسمى parseData
    public void parseData() {
        if (fullFileData == null) return;
        ByteBuffer bb = ByteBuffer.wrap(fullFileData).order(ByteOrder.LITTLE_ENDIAN);
        properties.clear();

        // هنا تضع حلقة الـ for loop التي تبحث عن "Property"
        // ... (كود المسح الخاص بك) ...
    }
    public void loadFromBytes(byte[] data) throws IOException {
        if (data == null || data.length == 0) {
            throw new IOException("المصفوفة فارغة، تعذر التحليل.");
        }

        this.fullFileData = data;
        ByteBuffer bb = ByteBuffer.wrap(this.fullFileData).order(ByteOrder.LITTLE_ENDIAN);
        properties.clear();

        // منطق الـ Scan الخاص بك بالكامل
        int limit = fullFileData.length - 24;
        for (int i = 0; i < limit; i++) {
            bb.position(i);
            String foundType = readUEString(bb);

            if (foundType != null && foundType.endsWith("Property")) {
                int typeEndPos = bb.position();

                for (int j = i - 4; j > i - 64 && j >= 0; j--) {
                    String possibleName = readUEStringAt(fullFileData, j);
                    if (possibleName != null && !possibleName.isEmpty() && (j + 4 + (possibleName.length() + 1) == i)) {
                        bb.position(typeEndPos);
                        try {
                            int size = bb.getInt();
                            int index = bb.getInt();
                            byte boolVal = bb.get();
                            int valueOffset = bb.position();

                            if (size >= 0 && bb.remaining() >= size) {
                                byte[] propertyData = new byte[size];
                                bb.get(propertyData);
                                Object value = parseValue(foundType, propertyData, boolVal);

                                properties.put(possibleName, new UEProperty(possibleName, foundType, propertyData, value, valueOffset));
//                                Log.d("SAV_SCAN", "تم العثور: " + possibleName + " [" + foundType + "] = " + value);
                            }
                        } catch (Exception e) {
                            Log.e("SAV_SCAN", "خطأ في قراءة داتا: " + possibleName);
                        }
                        break;
                    }
                }
            }
        }
    }
}