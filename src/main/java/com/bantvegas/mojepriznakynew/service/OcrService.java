package com.bantvegas.mojepriznakynew.service;

import net.sourceforge.tess4j.ITesseract;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;

@Service
public class OcrService {

    public String extractText(MultipartFile file) {
        ITesseract tesseract = new Tesseract();
        tesseract.setDatapath(new File("tessdata").getAbsolutePath()); // funguje lokálne aj pri deployi
        tesseract.setLanguage("slk"); // alebo "eng" podľa OCR jazykového modelu


        try {
            File tempFile = File.createTempFile("upload", file.getOriginalFilename());
            file.transferTo(tempFile);

            String result = tesseract.doOCR(tempFile);
            tempFile.delete();

            return result;
        } catch (IOException | TesseractException e) {
            e.printStackTrace();
            return "❌ OCR chyba: " + e.getMessage();
        }
    }
}
