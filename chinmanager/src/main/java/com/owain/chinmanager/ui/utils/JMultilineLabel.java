package com.owain.chinmanager.ui.utils;

import javax.swing.JLabel;
import javax.swing.JTextArea;
import javax.swing.border.EmptyBorder;
import javax.swing.text.DefaultCaret;

public class JMultilineLabel extends JTextArea
{
	private static final long serialVersionUID = 1L;

	public JMultilineLabel()
	{
		super();

		setEditable(false);
		setCursor(null);
		setOpaque(false);
		setFocusable(false);
		setWrapStyleWord(true);
		setLineWrap(true);
		setBorder(new EmptyBorder(0, 0, 0, 0));
		setAlignmentY(JLabel.CENTER_ALIGNMENT);

		DefaultCaret caret = (DefaultCaret) getCaret();
		if (caret != null)
		{
			caret.setUpdatePolicy(DefaultCaret.NEVER_UPDATE);
		}
	}
}
