package guidemo;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;

import javax.imageio.ImageIO;
import javax.swing.*;

/**
 * A frame that displays a multiline text, possibly with a background image
 * and with added icon images, in a DrawPanel, along with a variety of
 * controlls.
 */
public class GuiDemo extends JFrame {

	/**
	 * The main program just creates a GuiDemo frame and makes it visible.
	 */
	public static void main(String[] args) {
		JFrame frame = new GuiDemo();
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setVisible(true);
	}

	private DrawPanel drawPanel;
	private SimpleFileChooser fileChooser;
	private TextMenu textMenu;
	private JCheckBoxMenuItem gradientOverlayCheckbox = new JCheckBoxMenuItem("Gradient Overlay", true);

	/**
	 * The constructor creates the frame, sizes it, and centers it horizontally on
	 * the screen.
	 */
	public GuiDemo() {
		super("Sayings"); // Specifies the string for the title bar of the window.
		JPanel content = new JPanel(); // To hold the content of the window.
		content.setBackground(Color.LIGHT_GRAY);
		content.setLayout(new BorderLayout());
		setContentPane(content);

		// Create the DrawPanel that fills most of the window, and customize it.
		drawPanel = new DrawPanel();
		drawPanel.getTextItem().setText(
				"Fool me one time,\n" +
						"shame on you.\n" +
						"Fool me twice, \n" +
						"can't put the blame on you\n");
		drawPanel.getTextItem().setFontSize(36);
		drawPanel.getTextItem().setJustify(TextItem.LEFT);
		drawPanel.setBackgroundImage(Util.getImageResource("resources/images/earthrise.jpeg"));
		content.add(drawPanel, BorderLayout.CENTER);

		// Add an icon toolbar to the SOUTH position of the layout
		IconSupport iconSupport = new IconSupport(drawPanel);
		content.add(iconSupport.createToolbar(true), BorderLayout.SOUTH);

		// Add the icon toolbar to the SOUTH position of the layout
		content.add(iconSupport.createToolbar(true), BorderLayout.SOUTH);

		// Add the background toolbar to the NORTH position of the layout
		JToolBar backgroundToolbar = makeToolbar();
		content.add(backgroundToolbar, BorderLayout.NORTH);

		// Create the menu bar and add it to the frame.
		JMenuBar menuBar = new JMenuBar();

		// Add existing menus to the menu bar
		menuBar.add(makeFileMenu());
		textMenu = new TextMenu(drawPanel);
		menuBar.add(textMenu);
		menuBar.add(makeBackgroundMenu());

		// Add the new stamper menu created by IconSupport
		JMenu stamperMenu = iconSupport.createMenu();
		menuBar.add(stamperMenu);

		// Set the menu bar for the frame
		setJMenuBar(menuBar);

		// Set the size of the window and its position.
		pack(); // Size the window to fit its content.
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		setLocation((screenSize.width - getWidth()) / 2, 50);

		// Create and customize the file chooser that is used for file operations.
		fileChooser = new SimpleFileChooser();
		try { // I'd like to use the Desktop folder as the initial folder in the file chooser.
			String userDir = System.getProperty("user.home");
			if (userDir != null) {
				File desktop = new File(userDir, "Desktop");
				if (desktop.isDirectory())
					fileChooser.setDefaultDirectory(desktop);
			}
		} catch (Exception e) {
			// Exception handling if needed
		}
	}

	/**
	 * Create the "File" menu from actions that are defined later in this class.
	 */
	private JMenu makeFileMenu() {
		JMenu menu = new JMenu("File");
		menu.add(newPictureAction);
		menu.add(saveImageAction);
		menu.addSeparator();
		menu.add(quitAction);
		return menu;
	}

	/**
	 * Create the "Background" menu, using objects of type ChooseBackgroundAction,
	 * a class that is defined later in this file.
	 */
	private JMenu makeBackgroundMenu() {
		JMenu menu = new JMenu("Background");
		menu.add(new ChooseBackgroundAction("Mandelbrot"));
		menu.add(new ChooseBackgroundAction("Earthrise"));
		menu.add(new ChooseBackgroundAction("Sunset"));
		menu.add(new ChooseBackgroundAction("Cloud"));
		menu.add(new ChooseBackgroundAction("Eagle_nebula"));
		menu.addSeparator();
		menu.add(new ChooseBackgroundAction("Custom..."));
		menu.addSeparator();
		menu.add(new ChooseBackgroundAction("Color..."));
		menu.addSeparator();
		menu.add(gradientOverlayCheckbox);
		gradientOverlayCheckbox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				if (gradientOverlayCheckbox.isSelected())
					drawPanel.setGradientOverlayColor(Color.WHITE);
				else
					drawPanel.setGradientOverlayColor(null);
			}
		});
		return menu;
	}

	private AbstractAction newPictureAction = new AbstractAction("New",
			Util.iconFromResource("resources/action_icons/fileopen.png")) {
		public void actionPerformed(ActionEvent evt) {
			drawPanel.clear();
			gradientOverlayCheckbox.setSelected(true);
			textMenu.setDefaults();
		}
	};

	private AbstractAction quitAction = new AbstractAction("Quit",
			Util.iconFromResource("resources/action_icons/exit.png")) {
		public void actionPerformed(ActionEvent evt) {
			System.exit(0);
		}
	};

	private AbstractAction saveImageAction = new AbstractAction("Save Image...",
			Util.iconFromResource("resources/action_icons/filesave.png")) {
		public void actionPerformed(ActionEvent evt) {
			File f = fileChooser.getOutputFile(drawPanel, "Select Ouput File", "saying.jpeg");
			if (f != null) {
				try {
					BufferedImage img = drawPanel.copyImage();
					String format;
					String fileName = f.getName().toLowerCase();
					if (fileName.endsWith(".png"))
						format = "PNG";
					else if (fileName.endsWith(".jpeg") || fileName.endsWith(".jpg"))
						format = "JPEG";
					else {
						JOptionPane.showMessageDialog(drawPanel,
								"The output file name must end wth\n.png or .jpeg.");
						return;
					}
					ImageIO.write(img, format, f);
				} catch (Exception e) {
					JOptionPane.showMessageDialog(drawPanel, "Sorry, the image could not be saved.");
				}
			}
		}
	};

	/**
	 * An object of type ChooseBackgroudnAction represents an action through which
	 * the
	 * user selects the background of the picture. There are three types of
	 * background:
	 * solid color background ("Color..." command), an image selected by the user
	 * from
	 * the file system ("Custom..." command), and four built-in image resources
	 * (Mandelbrot, Earthrise, Sunset, and Eagle_nebula).
	 */
	private class ChooseBackgroundAction extends AbstractAction {
		String text;

		ChooseBackgroundAction(String text) {
			super(text);
			this.text = text;

			// Create icons for "Custom..." and "Color..." actions
			if (text.equals("Custom...")) {
				putValue(Action.SMALL_ICON,
						Util.iconFromResource("resources/action_icons/fileopen.png"));
				putValue(Action.SHORT_DESCRIPTION, "Select an image file to use as the background.");
			} else if (text.equals("Color...")) {
				ImageIcon colorIcon = createColorIcon();
				putValue(Action.SMALL_ICON, colorIcon);
				putValue(Action.SHORT_DESCRIPTION, "Use a solid color for background.");
			} else {
				putValue(Action.SMALL_ICON,
						Util.iconFromResource("resources/images/" + text.toLowerCase() + "_thumbnail.jpeg"));
				putValue(Action.SHORT_DESCRIPTION, "Use this image as the background.");
			}
		}

		private ImageIcon createColorIcon() {
			BufferedImage colorImage = new BufferedImage(32, 32, BufferedImage.TYPE_INT_RGB);
			Graphics g = colorImage.createGraphics();
			g.setColor(Color.DARK_GRAY);
			g.fillRect(0, 0, 32, 32);
			g.setColor(Color.CYAN);
			g.fillRect(8, 8, 16, 16);
			g.dispose();
			return new ImageIcon(colorImage);
		}

		public void actionPerformed(ActionEvent evt) {
			if (text.equals("Custom...")) {
				File inputFile = fileChooser.getInputFile(drawPanel, "Select Background Image");
				if (inputFile != null) {
					try {
						BufferedImage img = ImageIO.read(inputFile);
						if (img == null)
							throw new Exception();
						drawPanel.setBackgroundImage(img);
					} catch (Exception e) {
						JOptionPane.showMessageDialog(drawPanel, "Sorry, couldn't read the file.");
					}
				}
			} else if (text.equals("Color...")) {
				Color c = JColorChooser.showDialog(drawPanel, "Select Color for Background", drawPanel.getBackground());
				if (c != null) {
					drawPanel.setBackground(c);
					drawPanel.setBackgroundImage(null);
				}
			} else {
				Image bg = Util.getImageResource("resources/images/" + text.toLowerCase() + ".jpeg");
				drawPanel.setBackgroundImage(bg);
			}
		}
	}

	private JToolBar makeToolbar() {
		JToolBar toolbar = new JToolBar("Background Tools");
		toolbar.add(newPictureAction);
		toolbar.add(saveImageAction);
		toolbar.addSeparator();
		toolbar.add(new ChooseBackgroundAction("Mandelbrot"));
		toolbar.add(new ChooseBackgroundAction("Earthrise"));
		toolbar.add(new ChooseBackgroundAction("Sunset"));
		toolbar.add(new ChooseBackgroundAction("Cloud"));
		toolbar.add(new ChooseBackgroundAction("Eagle_nebula"));
		toolbar.addSeparator();
		toolbar.add(new ChooseBackgroundAction("Custom..."));
		toolbar.add(new ChooseBackgroundAction("Color..."));
		return toolbar;
	}

}
