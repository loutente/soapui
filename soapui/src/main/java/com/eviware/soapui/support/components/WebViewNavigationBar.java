/*
 *  SoapUI, copyright (C) 2004-2014 smartbear.com 
 *
 *  SoapUI is free software; you can redistribute it and/or modify it under the 
 *  terms of version 2.1 of the GNU Lesser General Public License as published by 
 *  the Free Software Foundation.
 *
 *  SoapUI is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without 
 *  even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 *  See the GNU Lesser General Public License for more details at gnu.org.
 */
package com.eviware.soapui.support.components;

import com.eviware.soapui.support.UISupport;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebHistory;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * @author joel.jonsson
 */
class WebViewNavigationBar
{
	private final JComponent toolbar;

	private WebEngine webEngine;
	private WebViewBasedBrowserComponent webViewBasedBrowserComponent;

	private JTextField urlField;
	private ForwardAction forwardAction;
	private BackAction backAction;
	private Color originalFontColor;

	WebViewNavigationBar( )
	{
		this.toolbar = createNavigationBar();
	}

	void initialize( final WebEngine webEngine, WebViewBasedBrowserComponent webViewBasedBrowserComponent )
	{
		this.webEngine = webEngine;
		this.webViewBasedBrowserComponent = webViewBasedBrowserComponent;

		webEngine.getHistory().currentIndexProperty().addListener( new ChangeListener<Number>()
		{
			@Override
			public void changed( ObservableValue<? extends Number> observableValue, Number oldHistoryIndex, Number newHistoryIndex )
			{
				if( backAction != null )
				{
					backAction.setEnabled( observableValue.getValue().intValue() > 0 );
				}
				if( forwardAction != null )
				{
					forwardAction.setEnabled( observableValue.getValue().intValue() < webEngine.getHistory().getEntries().size() - 1 );
				}
			}
		} );


		webEngine.locationProperty().addListener( new ChangeListener<String>()
		{
			@Override
			public void changed( ObservableValue<? extends String> observableValue, String oldLocation,
										final String newLocation )
			{
				if( urlField != null )
				{
					SwingUtilities.invokeLater( new Runnable()
					{
						public void run()
						{
							urlField.setText( newLocation );
							resetTextFieldDefaults();
							urlField.setFocusable( false );
							urlField.setFocusable( true );
						}
					} );
				}
			}
		} );
	}

	public void focusUrlField()
	{
		urlField.requestFocus();
	}

	private JComponent createNavigationBar()
	{
		JXToolBar toolbar = UISupport.createToolbar();
		urlField = new JTextField();
		backAction = new BackAction();
		backAction.setEnabled( false );
		toolbar.add( backAction );
		forwardAction = new ForwardAction();
		forwardAction.setEnabled( false );
		toolbar.add( forwardAction );
		toolbar.add( new ReloadAction() );
		toolbar.add( urlField );
		urlField.addActionListener( new UrlEnteredActionListener() );
		urlField.setText( "Enter URL here" );
		urlField.setFont( urlField.getFont().deriveFont( Font.ITALIC ) );
		originalFontColor = urlField.getForeground();
		urlField.setForeground( new Color( 170, 170, 170 ) );
		urlField.addKeyListener( new KeyAdapter()
		{
			@Override
			public void keyTyped( KeyEvent e )
			{
				removeHintText();
			}
		} );
		urlField.addMouseListener( new MouseAdapter()
		{
			@Override
			public void mouseClicked( MouseEvent e )
			{
				removeHintText();
			}
		} );
		return toolbar;
	}

	private void removeHintText()
	{
		if (urlField.getText().equals("Enter URL here"))
		{
			urlField.setText("");
			resetTextFieldDefaults();
		}
	}

	private void resetTextFieldDefaults()
	{
		urlField.setFont(urlField.getFont().deriveFont( Font.PLAIN ));
		urlField.setForeground( originalFontColor );
	}

	Component getComponent()
	{
		return toolbar;
	}

	private class BackAction extends AbstractAction
	{
		public BackAction()
		{
			putValue( SMALL_ICON, UISupport.createImageIcon( "/arrow_left.png" ) );
			putValue( Action.SHORT_DESCRIPTION, "Go back" );
		}

		public void actionPerformed( ActionEvent e )
		{
			if( webEngine == null ) return;
			final WebHistory history = webEngine.getHistory();
			if( history.getCurrentIndex() == 0 )
			{
				Toolkit.getDefaultToolkit().beep();
			}
			else
			{
				Platform.runLater( new Runnable()
				{
					@Override
					public void run()
					{
						history.go( -1 );
					}
				} );
			}
		}
	}

	private class ForwardAction extends AbstractAction
	{
		public ForwardAction()
		{
			putValue( SMALL_ICON, UISupport.createImageIcon( "/arrow_right.png" ) );
			putValue( Action.SHORT_DESCRIPTION, "Go forward" );
		}

		public void actionPerformed( ActionEvent e )
		{
			if( webEngine == null ) return;
			final WebHistory history = webEngine.getHistory();
			if( history.getCurrentIndex() >= history.getEntries().size() - 1 )
			{
				Toolkit.getDefaultToolkit().beep();
			}
			else
			{
				Platform.runLater( new Runnable()
				{
					@Override
					public void run()
					{
						history.go( 1 );
					}
				} );
			}
		}
	}

	private class ReloadAction extends AbstractAction
	{
		public ReloadAction()
		{
			putValue( SMALL_ICON, UISupport.createImageIcon( "/reload_properties.gif" ) );
			putValue( Action.SHORT_DESCRIPTION, "Reload page" );
		}

		public void actionPerformed( ActionEvent e )
		{
			if( webEngine == null ) return;
			Platform.runLater( new Runnable()
			{
				@Override
				public void run()
				{
					webEngine.reload();
				}
			} );
		}
	}

	private class UrlEnteredActionListener implements ActionListener
	{
		@Override
		public void actionPerformed( ActionEvent e )
		{
			String url = urlField.getText();
			if( !urlField.getText().contains( "://" ) )
			{
				url = "http://" + url;
			}
			webViewBasedBrowserComponent.navigate( url );
		}
	}
}
