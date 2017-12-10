/*
 * Copyright (C) 2017 Waleed Mortaja
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

import java.io.File;
import java.util.Locale;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

/**
 *
 * @author Waleed Mortaja, contact Email :
 * <a href="mailto:waleed.mortaja@gmail.com">waleed.mortaja@gmail.com</a>
 */
public class RenameFilesAsTheirOrder {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        JOptionPane.showMessageDialog(null, "هذا البرنامج يقوم بإعادة تسمة الملفات مثل"+": "+"الدرس الأول، الدرس الثاني ..."+"لتظهر بترتيبها الصحيح", "البداية", JOptionPane.PLAIN_MESSAGE);
        JFileChooser folderChooser = new JFileChooser(System.getProperty("user.home") + "/Desktop"); //default path is Desktop
        folderChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        if (folderChooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
            File folder = folderChooser.getSelectedFile();
            if (folder.exists() && folder.isAbsolute() && folder.canRead()) {
                File[] files = folder.listFiles(File::isFile); //list files only
                int numOfDigits = String.valueOf(files.length).length();
                for (File file : files) {
                    String[] fileNameParts = file.getName().split("(\\P{InArabic}+و+|\\P{InArabic}+)+");
                    file.renameTo(new File(file.getParentFile() + "\\" + getFileNumberAsItsOrder(fileNameParts, numOfDigits) + " " + file.getName()));
                }
                JOptionPane.showMessageDialog(null, "انتهت اعادة التسمية ","انتهى",JOptionPane.PLAIN_MESSAGE);
            }
        }
    }

    public static String getFileNumberAsItsOrder(String[] fileNameParts, int numOfDigits) {
        int result = 0;
        for (int i = 0; i < fileNameParts.length; i++) {
            String name = fileNameParts[i];
            if (name.length() > 2) {
                if ((name.charAt(0) == 'ا' || name.charAt(0) == 'أ') && name.charAt(1) == 'ل' && name.charAt(2) != 'ف') { //delete ال التعريف
                    name = name.substring(2);
                }
                if (name.charAt(name.length() - 1) == 'ه') {
                    name = name.substring(0, name.length() - 1) + "ة";
                }
                if (name.charAt(name.length() - 1) == 'ى') {
                    name = name.substring(0, name.length() - 1) + "ي";
                }
                name = name.replace('أ', 'ا').replace('إ', 'ا').replace('إ', 'ا').replace("ين", "ون").replace("ٍ", "").replaceAll("[ًٌٍَُِّْ،ـ]", "").replaceAll("ا$", "");//حذف الحركات أو بعض علامات الترقيم أو الألف في آخر الكلمة
                try {
                    Value val = Value.valueOf(name);
                    result += val.value();
                } catch (IllegalArgumentException ile) {
                    try {
                        int numberOfCase = 0;
                        switch (name) {
                            case "مئة":
                                numberOfCase = 100;
                                break;
                            case "الف":
                            case "الاف":
                                numberOfCase = 1000;
                                break;
                            case "مليون":
                            case "ملايون": //كل ين تم تحويلها الى ون
                                numberOfCase = 1000000;
                                break;
                            default:
                                name = name.substring(0, name.length() - 1); //حذف التاء المربوطة ل ثالثة على سبيل المثال
                                Value val = Value.valueOf(name);
                                result += val.value();
                                break;
                        }
                        if (numberOfCase > 0) { //do this instructions if numberOfCase has changed
                            if (result > 0) { // قيمة الرقم ليست أول كلمة  ... قيمة الرقم تعني مئة أو ألف أو مليون
                                if (i > 0 && fileNameParts[i - 1].equals("بعد")) {
                                    result += numberOfCase;
                                } else {
                                    int valueOfNumberOfCase = result % 1000; // الرقم الموجود ضمن هذه الخانة مثلا عندما يكون الرقم أحد عشر ألف فسيكون هذا الرقم أحد عشر
                                    result -= valueOfNumberOfCase; // طرح الثلاثة التي تمت اضافتها عند وجود رقم مثل ثلاثة آلاف
                                    result += valueOfNumberOfCase * numberOfCase;
                                }
                            } else { //قيمة الرقم هي أول كلمة
                                result += numberOfCase;
                            }
                        }
                    } catch (IllegalArgumentException ile2) {
                        //just ignore any word not in the enum 
                    }
                }

            }
        }
        String format = "%0" + numOfDigits + "d";
        return String.format(Locale.US,format, result);
    }

}
