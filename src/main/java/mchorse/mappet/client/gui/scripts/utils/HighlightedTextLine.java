package mchorse.mappet.client.gui.scripts.utils;

import mchorse.mappet.client.gui.utils.text.TextLine;
import net.minecraft.client.gui.FontRenderer;

import java.util.ArrayList;
import java.util.List;

public class HighlightedTextLine extends TextLine {
    public List<TextSegment> segments;
    public List<List<TextSegment>> wrappedSegments;

    public HighlightedTextLine(String text) {
        super(text);
    }

    public void resetSegments() {
        this.segments = null;
        this.wrappedSegments = null;
    }

    public void setSegments(List<TextSegment> segments) {
        this.segments = segments;
    }

    @Override
    public void resetWrapping() {
        super.resetWrapping();

        this.wrappedSegments = null;
    }

    @Override
    public void calculateWrappedLines(FontRenderer font, int w) {
        Object wrappedLines = this.wrappedLines;

        super.calculateWrappedLines(font, w);

        if (wrappedLines != this.wrappedLines) {
            this.resetSegments();
        }
    }

    public void calculateWrappedSegments(FontRenderer font) {
        if (this.wrappedLines == null) {
            this.wrappedSegments = null;
            return;
        }

        List<TextSegment> segments = new ArrayList<>();
        int w = 0;
        int i = 0;
        String line = this.wrappedLines.get(i);

        this.wrappedSegments = new ArrayList<>();

        for (TextSegment segment : this.segments) {
            int sw = segment.text.length();
            int total = w + sw;

            while (total > line.length()) {
                int endIndex = line.length() - w;

                TextSegment cutOff = new TextSegment(segment.token, segment.text.substring(0, endIndex), segment.color, segment.width);
                TextSegment remainder = new TextSegment(segment.token, segment.text.substring(endIndex), segment.color, segment.width);

                if (!cutOff.text.isEmpty()) {
                    cutOff.width = font.getStringWidth(cutOff.text);
                    segments.add(cutOff);
                }

                this.wrappedSegments.add(segments);

                segments = new ArrayList<>();
                segment = remainder;
                segment.width = font.getStringWidth(segment.text);

                sw = segment.text.length();
                w = 0;
                i += 1;

                if (i >= this.wrappedLines.size()) break;
                line = this.wrappedLines.get(i);
                if (remainder.text.isEmpty()) break;
                total = w + sw;
            }

            w += sw;
            segments.add(segment);
        }

        if (!segments.isEmpty()) this.wrappedSegments.add(segments);
    }
}