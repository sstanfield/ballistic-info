package transapps.ballistic.app.view;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;

import transapps.ballistic.app.Data;
import transapps.ballistic.app.Settings;
import transapps.ballistic.app.view.model.BallisticTableModel;
import transapps.ballistic.db.BallisticDBHelper;
import transapps.ballistic.db.dvo.AtmosphereDVO;
import transapps.ballistic.db.dvo.BulletDVO;
import transapps.ballistic.db.dvo.WeaponDVO;
import transapps.ballistic.lib.data.RangeData;
import transapps.ballistic.lib.data.Weapon;
import transapps.ballistic.lib.util.Conversions;

public class MainView extends JFrame implements UpdateViewInterface {
	private static final long serialVersionUID = 1L;
	private Settings s;
	private int start = 0;
	private int end = 3000;
	private int increment = 100;
	private final JTable table;

	private final OutputPanel outputPanel;
	private final WeaponPanel wepPanel;
	private final BulletPanel bulletPanel;
	private final WindPanel windPanel;
	private final AtmoPanel atmoPanel;
	private final ZeroPanel zeroPanel;
	private final AnglePanel anglePanel;
	private final CoriolisPanel coriolisPanel;
	private final TruingPanel truingPanel;
	private final JComboBox weapons;
	private final JButton btnDelete;

	private String newName(List<WeaponDVO> weapons, int n, String prefix, String postfix) {
		String ret = prefix+n+postfix;
		for (WeaponDVO w : weapons) {
			if (w.name.equals(ret)) {
				ret = newName(weapons, n+1, prefix, postfix);
				break;
			}
		}
		return ret;
	}
	private String newName(String prefix, String postfix) {
		List<WeaponDVO> weapons = WeaponDVO.allWeapons();
		return newName(weapons, 1, prefix, postfix);
	}

	private void refreshWepDrop() {
		int c = 0;
		for (Weapon w : Data.defaultWeapons) {
			if (w.name.equals(s.weapon.name)) {
				weapons.setSelectedIndex(c);
				break;
			}
			c++;
		}
	}

	private void newWep(WeaponDVO dvo) {
		dvo.save();
		weapons.setModel(Data.getWeaponComboBoxModel(0));
		int c = 0;
		for (Weapon w : Data.defaultWeapons) {
			if (w.name.equals(dvo.name)) {
				weapons.setSelectedIndex(c);
				s = s.newWeapon(w);
				break;
			}
			c++;
		}
		updateMach();
	}

	private void checkDefault() {
		boolean defWep = BallisticDBHelper.isDefaultWeapon(s.weapon);
		wepPanel.setEnabled(!defWep);
		bulletPanel.setEnabled(!defWep);
		btnDelete.setEnabled(!defWep);
	}

	private void updateMach() {
		double transonic = s.atmo.mach * 1.2;
		int tsr = 0;
		int sr = 0;
		int c = 0;
		for (RangeData r : s.table.getData()) {
			double v = s.imperial?r.getVelocity():Conversions.metersToFeet(r.getVelocity());
			if (v < transonic && tsr == 0) tsr = c;
			if (v < s.atmo.mach) {
				sr = c;
				break;
			}
			c++;
		}
		outputPanel.setMachs(tsr,  sr);
		truingPanel.setTransonic(s.imperial, tsr, s);
	}

	public MainView(final Settings settings) {
		super();
		this.s = settings;
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		setTitle("Ballistic Info: "+Data.defaultWeapons[0].name);
//		setSize(771, 556);

		GridBagLayout gridBagLayout = new GridBagLayout();
//		gridBagLayout.columnWidths = new int[]{771, 0};
//		gridBagLayout.rowHeights = new int[]{169, 69, 318, 0};
		gridBagLayout.columnWeights = new double[]{1.0, 1.0};
		gridBagLayout.rowWeights = new double[]{0.0, 0.0, 1.0, 0.0};
		getContentPane().setLayout(gridBagLayout);

		table = new JTable(new BallisticTableModel(s, start, end, increment));
		JScrollPane tableScrollpane = new JScrollPane(table);

		outputPanel = new OutputPanel(s.imperial, s.maxRange, increment, s.dropUnits, 
				s.spinDriftOn, this);

		JPanel buttonPanel = new JPanel();
		JButton newButton = new JButton("New Window");
		newButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				MainView mv = new MainView(s);
				mv.setVisible(true);
			}
		});
		buttonPanel.add(newButton);
		JButton btnQuit = new JButton("Close");
		btnQuit.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				dispose();
			}
		});
		buttonPanel.add(btnQuit);
		JButton btnCopy = new JButton("Copy Weapon");
		btnCopy.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Settings s = MainView.this.s;
				WeaponDVO dvo = new WeaponDVO(newName("Copy ", " of "+s.weapon.name), 
						s.weapon.description, s.weapon.velocity,
						s.weapon.sightHeight, s.weapon.rightTwist, 
						s.weapon.barrelTwist, s.weapon.zeroRange, 
						s.weapon.atmosphere, s.weapon.bullet);
				newWep(dvo);
			}
		});
		buttonPanel.add(btnCopy);
		JButton btnNew = new JButton("New Weapon");
		btnNew.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				WeaponDVO dvo = new WeaponDVO(newName("New Weapon ", ""), "", 2500,
						0, true, 0, 328.084, AtmosphereDVO.STANDARD, BulletDVO.DUMMY);
				newWep(dvo);
			}
		});
		buttonPanel.add(btnNew);
		btnDelete = new JButton("Delete Weapon");
		btnDelete.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				WeaponDVO dvo = new WeaponDVO(s.weapon);
				dvo.delete();
				weapons.setModel(Data.getWeaponComboBoxModel(0));
				weapons.setSelectedIndex(0);
			}
		});
		buttonPanel.add(btnDelete);

		zeroPanel = new ZeroPanel(s.imperial, s.weapon.zeroRange, s.weapon.atmosphere, this);

		JPanel ap = new JPanel();
		atmoPanel = new AtmoPanel(s.atmo, this);
		ap.add(atmoPanel);

		JPanel wndp = new JPanel();
		windPanel = new WindPanel(s.windSpeed, s.windDirection, s.windAngleUnit, s.mph, this);
		wndp.add(windPanel);

		weapons = new JComboBox(Data.getWeaponComboBoxModel(0));
		weapons.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JComboBox cb = (JComboBox)e.getSource();
				Weapon w = (Weapon)cb.getSelectedItem();
				s = s.newWeapon(w);
				setTitle("Ballistic Info: "+s.weapon.name);
				table.setModel(new BallisticTableModel(s, start, end, increment));
				wepPanel.setWeapon(s.weapon);
				bulletPanel.setBullet(s.weapon.bullet);
				zeroPanel.setData(s.imperial, s.weapon.zeroRange, s.weapon.atmosphere);
				checkDefault();
				updateMach();
			}
		});

		JPanel wp = new JPanel();
		wepPanel = new WeaponPanel(s.weapon, this);
		wp.add(wepPanel);
		bulletPanel = new BulletPanel(s.weapon.bullet, this);
		wp.add(bulletPanel);
		checkDefault();

		anglePanel = new AnglePanel(s.shootingAngle, this);
		coriolisPanel = new CoriolisPanel(s.latitude, s.azimuth, s.coriolisOn, this);
		truingPanel = new TruingPanel(s, this, wepPanel, bulletPanel);

		JTabbedPane tabbedPane = new JTabbedPane();
		ImageIcon icon = new ImageIcon(Toolkit.getDefaultToolkit().getImage(getClass().getResource("/images/menu_weapon.png")));
		tabbedPane.addTab("Weapon", icon, wp, "Weapon Settings");
		tabbedPane.setMnemonicAt(0, KeyEvent.VK_1);

		icon = new ImageIcon(Toolkit.getDefaultToolkit().getImage(getClass().getResource("/images/menu_zero.png")));
		tabbedPane.addTab("Zero", icon, zeroPanel, "Zero Settings");
		tabbedPane.setMnemonicAt(1, KeyEvent.VK_2);

		icon = new ImageIcon(Toolkit.getDefaultToolkit().getImage(getClass().getResource("/images/menu_wind.png")));
		tabbedPane.addTab("Wind", icon, wndp, "Windage Settings");
		tabbedPane.setMnemonicAt(2, KeyEvent.VK_3);

		icon = new ImageIcon(Toolkit.getDefaultToolkit().getImage(getClass().getResource("/images/menu_atmo.png")));
		tabbedPane.addTab("Atmosphere", icon, ap, "Atmosphere Settings");
		tabbedPane.setMnemonicAt(3, KeyEvent.VK_4);

		icon = new ImageIcon(Toolkit.getDefaultToolkit().getImage(getClass().getResource("/images/menu_angle.png")));
		tabbedPane.addTab("Angle", icon, anglePanel, "Angle Settings");
		tabbedPane.setMnemonicAt(4, KeyEvent.VK_5);

		icon = new ImageIcon(Toolkit.getDefaultToolkit().getImage(getClass().getResource("/images/menu_ce.png")));
		tabbedPane.addTab("Coriolis", icon, coriolisPanel, "Coriolis Settings");
		tabbedPane.setMnemonicAt(5, KeyEvent.VK_6);

		icon = new ImageIcon(Toolkit.getDefaultToolkit().getImage(getClass().getResource("/images/menu_true.png")));
		tabbedPane.addTab("Truing", icon, truingPanel, "True weapon.");
		tabbedPane.setMnemonicAt(6, KeyEvent.VK_7);

		GridBagConstraints gbc_buttonPanel = new GridBagConstraints();
		gbc_buttonPanel.insets = new Insets(0, 0, 5, 0);
		gbc_buttonPanel.fill = GridBagConstraints.VERTICAL;
		gbc_buttonPanel.anchor = GridBagConstraints.WEST;
		gbc_buttonPanel.gridx = 0;
		gbc_buttonPanel.gridy = 0;
		getContentPane().add(buttonPanel, gbc_buttonPanel);

		GridBagConstraints gbc_wepsPanel = new GridBagConstraints();
		gbc_wepsPanel.insets = new Insets(0, 0, 5, 0);
		gbc_wepsPanel.fill = GridBagConstraints.BOTH;
		gbc_wepsPanel.gridx = 0;
		gbc_wepsPanel.gridy = 1;
		getContentPane().add(weapons, gbc_wepsPanel);

		GridBagConstraints gbc_tabsPanel = new GridBagConstraints();
		gbc_tabsPanel.insets = new Insets(0, 0, 5, 0);
		gbc_tabsPanel.fill = GridBagConstraints.BOTH;
		gbc_tabsPanel.gridx = 0;
		gbc_tabsPanel.gridy = 2;
		getContentPane().add(tabbedPane, gbc_tabsPanel);

		GridBagConstraints gbc_outPanel = new GridBagConstraints();
		gbc_outPanel.insets = new Insets(0, 0, 5, 0);
		gbc_outPanel.fill = GridBagConstraints.BOTH;
		gbc_outPanel.gridx = 0;
		gbc_outPanel.gridy = 3;
		getContentPane().add(outputPanel, gbc_outPanel);

		GridBagConstraints gbc_tableScrollpane = new GridBagConstraints();
		gbc_tableScrollpane.weighty = 1.0;
		gbc_tableScrollpane.gridwidth = 2;
		gbc_tableScrollpane.anchor = GridBagConstraints.NORTH;
		gbc_tableScrollpane.fill = GridBagConstraints.BOTH;
		gbc_tableScrollpane.gridx = 0;
		gbc_tableScrollpane.gridy = 4;
		getContentPane().add(tableScrollpane, gbc_tableScrollpane);

		updateMach();
		pack();
	}

	public void updateView() {
		Weapon w = wepPanel.getWeapon();
		w = w.newBullet(bulletPanel.getBullet());
		w = w.newZero(zeroPanel.getRange(), zeroPanel.getAtmo());
		WeaponDVO w2 = new WeaponDVO(w);
		w2.update();
		weapons.setModel(Data.getWeaponComboBoxModel(0));
		end = outputPanel.getMax();
		increment = outputPanel.getIncrement();
		s = new Settings(end, outputPanel.getDrop(), anglePanel.getAngle(),
				coriolisPanel.isOn(), coriolisPanel.getLatitude(), coriolisPanel.getAzimuth(),
				w, atmoPanel.getAtmo(), outputPanel.isSpinDrift(), windPanel.getSpeed(),
				windPanel.getDirection(), windPanel.getAngleUnit(),
				outputPanel.isImperial(), windPanel.isMph());
		table.setModel(new BallisticTableModel(s, start, end, increment));
		zeroPanel.setImperial(outputPanel.isImperial());
		updateMach();
		refreshWepDrop();
	}
}
