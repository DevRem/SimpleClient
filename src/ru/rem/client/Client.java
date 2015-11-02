package ru.rem.client;

import java.awt.Color;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import javax.swing.DefaultListModel;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.SwingConstants;
import javax.swing.Timer;
import javax.swing.UIManager;
import javax.swing.WindowConstants;
import ru.rem.server.session.actions.ActionDownload;
import ru.rem.server.session.actions.ActionUpload;
import ru.rem.server.session.packets.AbstractPacket;
import ru.rem.server.session.packets.data.PacketStatus;

public class Client extends JFrame{
  
    private JButton jDownload;
    private JButton jAdd;
    private JLabel jServer;
    private JLabel jServerStatus;
    private JLabel jSelectedFiles;
    private JList jServerFiles;
    private JList jFiles;
    private JMenu jMenu;
    private JMenuBar jMenuBar;
    private JMenuItem jMenuItemConnect;
    private JMenuItem jMenuItemDisconect;
    private JScrollPane jSpServerFiles;
    private JScrollPane jSpFiles;
    private JProgressBar jProgressBar;
    private final ServerHandler server = new ServerHandler(this);
    private final DefaultListModel fileList = new DefaultListModel();
    private final DefaultListModel serverFileList = new DefaultListModel();
    
    public Client() {
        super("Клиент");
        initComponents();
    }
    
    private void initComponents() {
        
        jDownload = new JButton();
        jAdd = new JButton();
        
        jMenu = new JMenu();
        jMenuBar = new JMenuBar();
        jMenuItemConnect = new JMenuItem();
        jMenuItemDisconect = new JMenuItem();
        
        jServer = new JLabel();
        jServerStatus = new JLabel();
        jSelectedFiles = new JLabel();
        
        jProgressBar = new JProgressBar();
        
        jSpServerFiles = new JScrollPane();
        jSpFiles = new JScrollPane();
        jServerFiles = new JList(serverFileList);
        jFiles = new JList(fileList);

        jDownload.setText("Скачать");
        jAdd.setText("Добавить");
        
        jServerStatus.setText("Отключен");
        jServerStatus.setForeground(new Color(255, 51, 51));
        jServer.setText("Статус:");
        jSelectedFiles.setText("Выбранные файлы:");
        jMenu.setText("Сервер");
        jMenuItemConnect.setText("Соединиться");
        jMenuItemDisconect.setText("Отключиться");
        jProgressBar.setToolTipText("Наименование и размер файла");
        jProgressBar.setMaximum(100);
        jServerStatus.setHorizontalAlignment(SwingConstants.LEFT);
        jSpServerFiles.setViewportView(jServerFiles);
        jSpFiles.setViewportView(jFiles);

        jMenu.add(jMenuItemConnect);
        jMenu.add(jMenuItemDisconect);
        
        jMenuBar.add(jMenu);
        setJMenuBar(jMenuBar);
        
        jProgressBar.setStringPainted(true);
        jProgressBar.setMinimum(0);
        setForeground(Color.black);
        setBackground(Color.lightGray);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setResizable(false);
        actionsPerformed();
        
        GroupLayout layout = new GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            
            layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)    
                .addGroup(layout.createSequentialGroup()
                    .addGap(12, 12, 12)
                    .addComponent(jServer)
                    .addGap(8, 8, 8)
                    .addComponent(jServerStatus)
                    .addPreferredGap(ComponentPlacement.RELATED)
                    .addGap(325, 325, 325)
                    .addComponent(jSelectedFiles))
                .addGroup(layout.createSequentialGroup()
                    .addGap(10, 10, 10)
                    .addComponent(jSpServerFiles, GroupLayout.PREFERRED_SIZE, 270, GroupLayout.PREFERRED_SIZE)
                    .addGap(6, 6, 6)
                    .addComponent(jSpFiles, GroupLayout.PREFERRED_SIZE, 270, GroupLayout.PREFERRED_SIZE))
                .addGroup(layout.createSequentialGroup()
                        .addGap(10, 10, 10)
                        .addComponent(jProgressBar, GroupLayout.PREFERRED_SIZE, 547, GroupLayout.PREFERRED_SIZE))
                .addGroup(layout.createSequentialGroup()
                    .addGap(200, 200, 200)
                    .addComponent(jDownload, GroupLayout.PREFERRED_SIZE, 80, GroupLayout.PREFERRED_SIZE)
                    .addGap(195, 195, 195)
                    .addComponent(jAdd)))
                    
                .addContainerGap(10, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                    .addComponent(jServer)
                    .addComponent(jServerStatus)
                    .addComponent(jSelectedFiles))
                .addGap(1, 1, 1)
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                    .addComponent(jSpServerFiles, GroupLayout.PREFERRED_SIZE, 309, GroupLayout.PREFERRED_SIZE)
                    .addComponent(jSpFiles, GroupLayout.PREFERRED_SIZE, 309, GroupLayout.PREFERRED_SIZE))
                .addGap(1, 1, 1)
                .addComponent(jProgressBar, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                .addGap(1, 1, 1)
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                    .addComponent(jDownload)
                    .addComponent(jAdd).addGap(30, 30, 30)))
                .addGap(3, 3, 3)
        );

        pack();
    }
    
    public void actionsPerformed(){
        
        jAdd.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                JFileChooser dialog = updateUI(new JFileChooser());
                dialog.setFileSelectionMode(JFileChooser.FILES_ONLY);
                dialog.setDialogTitle("Выберите файл(ы) для загрузки");
                dialog.setMultiSelectionEnabled(true);
                int ret = dialog.showDialog(null, "Открыть файл");
                if (ret == JFileChooser.APPROVE_OPTION) {
                    File[] files = dialog.getSelectedFiles();
                    for (File file: files) {
                        ActionUpload packet = new ActionUpload(PacketStatus.PROCESSED, file);
                        addElementFile(packet);
                    }
                }
            }
        });
        
        jDownload.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                AbstractPacket selectedPacket = (AbstractPacket) jServerFiles.getSelectedValue();
                if(selectedPacket != null){
                    JFileChooser dialog = updateUI(new JFileChooser());
                    dialog.setDialogTitle("Сохранить...");
                    dialog.setSelectedFile(new File(selectedPacket.toString()));
                    if (dialog.showSaveDialog(null) == JFileChooser.APPROVE_OPTION) {
                        File file = dialog.getSelectedFile();
                        ActionDownload packet = new ActionDownload(PacketStatus.PROCESSED, file, selectedPacket.toString());
                        addElementFile(packet);
                    }
                }
            }
        });
        
        jMenuItemConnect.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                connect();
            }
        });
        
        jMenuItemDisconect.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                disconect();
            }
        });
        
    }
    
    public final void refreshConn(boolean isConnected){
        if (isConnected) {
            jServerStatus.setText("Подключен");
            jServerStatus.setForeground(new Color(51, 153, 0));
        } else {
            serverFileList.clear();
            jServerStatus.setText("Отключен");
            jServerStatus.setForeground(new Color(255, 51, 51));
        }
        
    }
    
    public void connect(){
        try {
            server.сonnect();
        } catch (IOException ex) {
            jServerStatus.setText("Ошибка подключения");
            jServerStatus.setForeground(new Color(255, 51, 51));
        }
    }
    
    public void disconect(){
        server.close();
    }
    
    public void addElementFile(AbstractPacket packet){
        fileList.addElement(packet);
        server.addPacketToQueue(packet);
    }
    
    public synchronized void addElementServerFile(AbstractPacket packet){        
        serverFileList.addElement(packet);
    }
    
    public void removeElement(AbstractPacket packet){
        if(server.removePacket(packet)){
            fileList.removeElement(packet);
        }
    }
    
    public void updatePrBar(int val){
        jProgressBar.setValue(val);
    }
    
    public void showMessage(String message){
        JOptionPane pane = new JOptionPane(message, JOptionPane.INFORMATION_MESSAGE);
        final JDialog dialog = pane.createDialog(this, "Сообщение");
        Timer timer = new Timer(2000, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dialog.setVisible(false);
                dialog.dispose();
            }
        });
        timer.setRepeats(false);
        timer.start();
        dialog.setVisible(true);
    }
    
    public synchronized void removeElementServerFile(String target) {
        serverFileList.removeElement(target);
    }
    
    public static void main(String[] args){
        
        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (Exception  ex) {
        }
        
        EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                new Client().setVisible(true);
            }
        });
        
    }
    
    public JFileChooser updateUI(JFileChooser choose) {
       
        UIManager.put("FileChooser.openButtonText", "Открыть");
        UIManager.put("FileChooser.cancelButtonText", "Отмена");
        UIManager.put("FileChooser.lookInLabelText", "Смотреть в");
        UIManager.put("FileChooser.fileNameLabelText", "Имя файла");
        UIManager.put("FileChooser.filesOfTypeLabelText", "Тип файла");

        UIManager.put("FileChooser.saveButtonText", "Сохранить");
        UIManager.put("FileChooser.saveButtonToolTipText", "Сохранить");
        UIManager.put("FileChooser.openButtonText", "Открыть");
        UIManager.put("FileChooser.openButtonToolTipText", "Открыть");
        UIManager.put("FileChooser.cancelButtonText", "Отмена");
        UIManager.put("FileChooser.cancelButtonToolTipText", "Отмена");

        UIManager.put("FileChooser.lookInLabelText", "Папка");
        UIManager.put("FileChooser.saveInLabelText", "Папка");
        UIManager.put("FileChooser.fileNameLabelText", "Имя файла");
        UIManager.put("FileChooser.filesOfTypeLabelText", "Тип файлов");

        UIManager.put("FileChooser.upFolderToolTipText", "На один уровень вверх");
        UIManager.put("FileChooser.newFolderToolTipText", "Создание новой папки");
        UIManager.put("FileChooser.listViewButtonToolTipText", "Список");
        UIManager.put("FileChooser.detailsViewButtonToolTipText", "Таблица");
        UIManager.put("FileChooser.fileNameHeaderText", "Имя");
        UIManager.put("FileChooser.fileSizeHeaderText", "Размер");
        UIManager.put("FileChooser.fileTypeHeaderText", "Тип");
        UIManager.put("FileChooser.fileDateHeaderText", "Изменен");
        UIManager.put("FileChooser.fileAttrHeaderText", "Атрибуты");
        UIManager.put("FileChooser.acceptAllFileFilterText", "Все файлы");
        
        choose.updateUI();
        return choose;
    }
    
}
