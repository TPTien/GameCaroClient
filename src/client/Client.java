package client;

import java.io.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

import java.net.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.border.MatteBorder;
import javax.swing.border.TitledBorder;

public class Client implements Runnable{
    private ImageIcon  xIcon= new ImageIcon(this.getClass().getResource("/Images/x.png"));
    private ImageIcon oIcon = new ImageIcon(this.getClass().getResource("/Images/o.png"));
    
    public static JFrame f;
    JButton[][] bt;
    boolean isPlaying = false;
    boolean winner;
    private boolean running =false;
    
    ButtonGroup btnGroup;
    JRadioButton rd_x,rd_o;
    JTextArea content;
    JTextField nhap,enterchat;
    JButton send,btnFindMatch;
    Timer thoigian;
    JLabel demthoigian,labelYou,labelOpponent;
    TextField textField;
    JPanel p,panelChosing,panelInforMatch;
    String temp = "";
    String strNhan = "";
    int xx, yy, x, y;
    int[][] matran;
    int[][] matrandanh;
    Integer THIRDTY_SECONDS=30000;
    
    
    // Server Socket
    //ServerSocket serversocket;
    Socket socket;
    OutputStream os;// ....
    InputStream is;// ......
    static ObjectOutputStream oos;// .........
    static ObjectInputStream ois;// 
    Input input;
    
    //MenuBar
    MenuBar menubar;
    private String userName;
    private String opponentName;
    
    public Client(String userName) {
        
        this.userName= userName;
        f = new JFrame();
        f.setTitle("Game Caro Client");
        f.setSize(980, 720);
        x = 25;
        y = 25;
        f.getContentPane().setLayout(null);
        f.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        //f.setVisible(true);
        f.setResizable(false);
        
        matran = new int[x][y];
        matrandanh = new int[x][y];
        menubar = new MenuBar();
        p = new JPanel();
        p.setBounds(10, 40, 600, 600);
        p.setBorder(BorderFactory.createStrokeBorder(new BasicStroke(5.0f),Color.BLUE));
        p.setLayout(new GridLayout(x, y));
        
        f.add(p);
        
        f.setMenuBar(menubar);// tao menubar cho frame
        Menu game = new Menu("Game");
        menubar.add(game);
        Menu help = new Menu("Help");
        menubar.add(help);
        MenuItem helpItem = new MenuItem("Help");
        help.add(helpItem);
        MenuItem about = new MenuItem("About ..");
        help.add(about);
        help.addSeparator();
        MenuItem newItem = new MenuItem("New Game");
        game.add(newItem);
        MenuItem exit = new MenuItem("Exit");
        game.add(exit);
        game.addSeparator();
        
        f.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                 if (JOptionPane.showConfirmDialog(f, 
                    "Bạn thật sự muốn thoát?", "Thoát game?", 
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION){
                     try {
                         oos.writeObject("exitgame,123,123,"+opponentName);
                     } catch (IOException ex) {
                         ex.printStackTrace();
                     }
                    System.exit(0);
                }
            }
            
        });
        newItem.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent e) {
                        newgame();
                        try {
                                oos.writeObject("newgame,123,123,"+opponentName);
                        } catch (IOException ie) {
                                //
                                
                        }
                }

        });
        exit.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent e) {
                    try {
                        oos.writeObject("exitgame,123,123,"+opponentName);
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }finally{
                        System.exit(0);
                    }
                        
                }
        });
        about.addActionListener(new ActionListener()
        {
            @Override
                public void actionPerformed(ActionEvent e) {
                    //Object[] options = {"OK"};
                 JOptionPane.showConfirmDialog(f,
                                "Trần Phước Tiến_1620253\nTrương Minh Sĩ_1620209\nCao Bảo Hiên_1620075", "Information",
                                JOptionPane.CLOSED_OPTION); 
                }
        });
        help.addActionListener(new ActionListener()
        {
            @Override
                public void actionPerformed(ActionEvent e) {
                    //Object[] options = {"OK"};
                 JOptionPane.showConfirmDialog(f,
                                "Luật chơi rất đơn giản bạn chỉ cần 5 ô liên tiếp nhau\n"
                                        + "Theo hàng ngang hoặc dọc hoặc chéo là bạn đã thắng\n"+"Chặn 2 đầu vẫn tính thắng", "Luật Chơi",
                                JOptionPane.CLOSED_OPTION); 
                }
        });
        //
//        btnLogin = new JButton("Click to login");
//        btnLogin.setBounds(600,0,200,50);
//        btnLogin.addActionListener(
//                new ActionListener(){
//                    public void actionPerformed(ActionEvent e) {
//                        LoginDialog loginDlg = new LoginDialog(f);
//                        loginDlg.setVisible(true);
//                        // if logon successfully
//                        if(loginDlg.isSucceeded()){
//                            btnLogin.setText("Hi " + loginDlg.getUsername() + "!");
//                        }
//                    }
//                });
//        f.add(btnLogin);
        btnFindMatch =new JButton("Tìm trận");
        btnFindMatch.setFont(new Font("Serif", Font.PLAIN, 18));
        btnFindMatch.setBackground(Color.BLUE);
        //btnFindMatch.setOpaque(true);
        btnFindMatch.setForeground(Color.white);
        btnFindMatch.setBounds(250,0,100,35);
        btnFindMatch.setBorder(BorderFactory.createTitledBorder(BorderFactory.createStrokeBorder(new BasicStroke(1.0f),Color.BLACK)));
        btnFindMatch.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    oos.writeObject("waiting,123,123,"+userName);
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        });
        //
        panelInforMatch = new JPanel();
        panelInforMatch.setLayout(null);
        panelInforMatch.setBorder(BorderFactory.createTitledBorder(BorderFactory.createStrokeBorder(new BasicStroke(2.0f),Color.BLACK),"Thông tin trận đấu",TitledBorder.CENTER, TitledBorder.TOP, new Font("times new roman",Font.PLAIN,16), Color.BLACK));
        labelYou =new JLabel("Bạn");
        labelYou.setBounds(20,45,120,120);
        labelYou.setFont(new Font("Serif", Font.PLAIN, 18));
        //labelYou.setForeground(Color.WHITE);
        labelYou.setBackground(Color.GREEN);
        labelYou.setOpaque(true);
        labelYou.setHorizontalAlignment(SwingConstants.CENTER);
        labelYou.setBorder(BorderFactory.createStrokeBorder(new BasicStroke(2.0f),Color.BLACK));
        
        labelOpponent =new JLabel("Đối thủ");
        labelOpponent.setBounds(160,45,120,120);
        //labelOpponent.setForeground(Color.WHITE);
        labelOpponent.setBackground(Color.red);
        labelOpponent.setFont(new Font("Serif", Font.PLAIN, 18));
        labelOpponent.setOpaque(true);
        labelOpponent.setHorizontalAlignment(SwingConstants.CENTER);
        labelOpponent.setBorder(BorderFactory.createStrokeBorder(new BasicStroke(2.0f),Color.BLACK));
        
        panelInforMatch.setBounds(630,100,300,200);
        panelInforMatch.add(labelYou);
        panelInforMatch.add(labelOpponent);
        f.add(panelInforMatch);
        
        //
        f.add(btnFindMatch);
        //
        panelChosing =new JPanel(null);
        panelChosing.setBounds(630,40,300,60);
        panelChosing.setBorder(BorderFactory.createTitledBorder(BorderFactory.createStrokeBorder(new BasicStroke(2.0f),Color.BLACK),"Chọn cờ",TitledBorder.CENTER, TitledBorder.TOP, new Font("times new roman",Font.PLAIN,16), Color.BLACK));
        btnGroup=new ButtonGroup();
        
        rd_o=new JRadioButton("O",true);
        rd_o.setHorizontalAlignment(SwingConstants.CENTER);
        rd_o.setBounds(50,25,45,20);
        rd_o.setFont(new java.awt.Font("Serif", Font.PLAIN, 18));
        
        //cb_o.setIcon(oIcon);
        rd_x =new JRadioButton("X");
        rd_x.setFont(new java.awt.Font("Serif", Font.PLAIN, 18));
        rd_x.setBounds(200,25,45,20);
        //cb_x.setIcon(xIcon);
        btnGroup.add(rd_o);
        btnGroup.add(rd_x);
        
        panelChosing.add(rd_o);
        panelChosing.add(rd_x);
        f.add(panelChosing);
        
        //khung chat
        
        Font fo = new Font("Serif",Font.PLAIN,16);
        content = new JTextArea();
        content.setFont(fo);
        content.setBackground(Color.LIGHT_GRAY);
//        content.setMargin(new Insets(10,10,10,10));
        content.setBorder(BorderFactory.createStrokeBorder(new BasicStroke(2.0f),Color.BLACK));
        
        
        content.setEditable(false);
        JScrollPane sp = new JScrollPane(content);
        sp.setBounds(630,310,300,280);
        send = new JButton("Gửi");
        send.setBounds(860, 610, 70, 30);
        send.setBackground(Color.CYAN);
        send.setFont(fo);
        send.setBorder(BorderFactory.createStrokeBorder(new BasicStroke(2.0f),Color.BLACK));
        nhap = new JTextField(30);
        nhap.setFont(fo);
        enterchat = new JTextField("");
        enterchat.setFont(fo);
        enterchat.setBounds(630, 610, 220, 30);
        enterchat.setBorder(BorderFactory.createStrokeBorder(new BasicStroke(2.0f),Color.BLACK));
        enterchat.setBackground(Color.white);
        //content.setVisible(false);
        send.addActionListener(new ActionListener(){
                @Override
                public void actionPerformed(ActionEvent e) {
                    if(e.getSource().equals(send))
                {
                    try
                    {
                    if(!enterchat.getText().isEmpty()){
                        temp+="Bạn: " + enterchat.getText() + "\n";
                        content.setText(temp);
                        oos.writeObject("chat," + enterchat.getText()+","+"123,"+opponentName);
                        enterchat.setText("");
                        //temp = "";
                        //System.out.println("clicked");
                        enterchat.requestFocus();
                        content.update(content.getGraphics());
                        content.setVisible(true);

                    }


                }
                catch (Exception r)
                {
                    r.printStackTrace();
                }
        }
    }
    });
        content.setVisible(true);
        f.add(enterchat);
        f.add(send);
        f.add(sp);
        f.setVisible(true);
        
        
        
        demthoigian = new JLabel("00:00");
        demthoigian.setHorizontalAlignment(SwingConstants.CENTER);
        demthoigian.setFont(new Font("TimesRoman", Font.ITALIC, 16));
        demthoigian.setForeground(Color.BLACK);
        demthoigian.setBorder(BorderFactory.createStrokeBorder(new BasicStroke(2.0f),Color.BLACK));
        f.add(demthoigian);
        demthoigian.setBounds(500,0,80,30);
        
        
        thoigian = new Timer(1000, new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent e) {
                        
                        THIRDTY_SECONDS -=1000;
                        if (THIRDTY_SECONDS == 0 ) {
                                try {
                                        oos.writeObject("youwin,123,123,"+opponentName);
                                } catch (IOException ex) 
                                              {
                                }
                                showMessageDialog("thua");
                                
                        } else {
                                demthoigian.setText( "00:" + THIRDTY_SECONDS/1000);
                                
                        }

                                }

        });
        
        bt = new JButton[x][y];
        for(int i = 0; i < x; i++)
        {
            for(int j = 0; j < y; j++)
            {
                final int a = i, b =j;
                bt[a][b] = new JButton();
                bt[a][b].setBackground(Color.LIGHT_GRAY);
                bt[a][b].setContentAreaFilled(false);
                bt[a][b].addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                        isPlaying = true;// server da click
                        thoigian.start();
                        matran[a][b] = 1;
                         
                        bt[a][b].setEnabled(false); 
//                        bt[a][b].setIcon(oIcon);
//                        
//                        bt[a][b].setDisabledIcon(xIcon);
                        //bt[a][b].setBackground(Color.BLACK);
                        checkChosingTypeChess(bt[a][b], userName);
                        labelOpponent.setBorder(new MatteBorder(1,1,4,1,Color.BLACK));
                        labelYou.setBorder(null);
                        //matran[a][b]=1;
                        //checkWin();
                      
                        try{
                            oos.writeObject("caro," + a + "," + b+","+opponentName+","+userName);
                            System.out.println(opponentName);
                            
                            setEnableButton(false);
                            checkWin();
                        }
                        catch(Exception ie)
                        {
                            ie.printStackTrace();
                        }
                        thoigian.stop();
                  }

                });                
                p.add(bt[a][b]);
                p.setVisible(false);
                p.setVisible(true);
            }
        }
        setEnableButton(false);
        send.setEnabled(false);
        try {
                  connect();
//                    }
            } catch (Exception ie) {
                    // ie.printStackTrace();
            }
        
    }
    private void checkChosingTypeChess(JButton button,String player){
        if(player.equals(userName)){
            if(rd_o.isSelected()){

                button.setIcon(oIcon);
                button.setDisabledIcon(oIcon);
            }
            else if(rd_x.isSelected()){

                button.setIcon(xIcon);
                button.setDisabledIcon(xIcon);
            }
        }
        else{
            if(rd_o.isSelected()){

                button.setIcon(xIcon);
                button.setDisabledIcon(xIcon);
            }
            else if(rd_x.isSelected()){

                button.setIcon(oIcon);
                button.setDisabledIcon(oIcon);
            }
        }
       
        
    }

    public void connect(){
        try {
                socket = new Socket("127.0.0.1",1234);
                System.out.println("Da ket noi toi server!");
                os=socket.getOutputStream();
                is=socket.getInputStream();
                oos= new ObjectOutputStream(os);
                ois= new ObjectInputStream(is);
                input =new Input(ois);
                oos.writeObject("login,123,123,"+userName);
                
                new Thread(input).start();
        } catch (Exception e) {
                e.printStackTrace();
        }
    }
    public void close(){
        try {
            running = false;
            ois.close();
            oos.close();
            socket.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        
    }
    public void newgame() {
         for (int i = 0; i < x; i++)
         {
                 for (int j = 0; j < y; j++) {
                         bt[i][j].setBackground(Color.LIGHT_GRAY);
                         bt[i][j].setIcon(null);
                         matran[i][j] = 0;
                         matrandanh[i][j] = 0;
                 }
         }
         rd_o.setEnabled(true);
         rd_x.setEnabled(true);
         //setEnableButton(true);
         THIRDTY_SECONDS =30000;
         thoigian.stop();
         isPlaying=false;
    }
       
    public void setVisiblePanel(JPanel pHienthi) {
         f.add(pHienthi);
         pHienthi.setVisible(true);
         pHienthi.updateUI();// ......

    }
    public void showMessageDialog(String msg){
        int m = JOptionPane.showConfirmDialog(f,
    "Bạn đã "+msg+". Bạn có muống chơi lại không?", "Thông báo",
    JOptionPane.YES_NO_OPTION);
        if (m == JOptionPane.YES_OPTION) {
                setVisiblePanel(p);
                newgame();
                setEnableButton(true);
                try {
                        oos.writeObject("newgame,123,123,"+opponentName);
                } catch (IOException ie) {
                    ie.printStackTrace();
                        //
                }
        } else if(m == JOptionPane.NO_OPTION){
            try {
                oos.writeObject("exitroom,123,123,"+opponentName);
                setEnableButton(false);
                labelOpponent.setText("Đối thủ");
                labelOpponent.setBorder(null);
                labelYou.setBorder(null);
                newgame();

            } catch (IOException ex) {
                ex.printStackTrace();
            }
            finally{
                //close();
                //System.exit(0);
                thoigian.stop();
            }

        }
    }
    
    public void setEnableButton(boolean b) {
            for (int i = 0; i < x; i++)
            {
                    for (int j = 0; j < y; j++) {
                            if (matran[i][j] == 0 && matrandanh[i][j]==0)
                                    bt[i][j].setEnabled(b);
                    }
            }
    }
    
    //thuat toan tinh thang thua
    public int checkHang() {
            int win = 0, hang = 0, n = 0, k = 0;
            boolean check = false;
            for (int i = 0; i < x; i++) {
                    for (int j = 0; j < y; j++) {
                            if (check) {
                                    if (matran[i][j] == 1) {
                                            hang++;
                                            if (hang > 4) {
//                                                if(matran[i][j]==0)
                                                    win = 1;
                                                    break;
                                            }
                                            continue;
                                    } else {
                                            check = false;
                                            hang = 0;
                                    }
                            }
                            if (matran[i][j] == 1) {
                                    check = true;
                                    hang++;
                            } else {
                                    check = false;
                            }
                    }
                    hang = 0;
            }
            return win;
    }

    public int checkCot() {
            int win = 0, cot = 0;
            boolean check = false;
            for (int j = 0; j < y; j++) {
                    for (int i = 0; i < x; i++) {
                            if (check) {
                                    if (matran[i][j] == 1) {
                                            cot++;
                                            if (cot > 4) {
                                                    win = 1;
                                                    break;
                                            }
                                            continue;
                                    } else {
                                            check = false;
                                            cot =0;
                                    }
                            }
                            if (matran[i][j] == 1) {
                                    check = true;
                                    cot++;
                            } else {
                                    check = false;
                            }
                    }
                    cot = 0;
            }
            return win;
    }

    public int checkCheoPhai() {
            int win = 0, cheop = 0, n = 0, k = 0;
            boolean check = false;
            for (int i = x - 1; i >= 0; i--) {
                    for (int j = 0; j < y; j++) {
                            if (check) {
                                    if (matran[n - j][j] == 1) {
                                            cheop++;
                                            if (cheop > 4) {
                                                    win = 1;
                                                    break;
                                            }
                                            continue;
                                    } else {
                                            check = false;
                                            cheop = 0;
                                    }
                            }
                            if (matran[i][j] == 1) {
                                    n = i + j;
                                    check = true;
                                    cheop++;
                            } else {
                                    check = false;
                            }
                    }
                    cheop = 0;
                    check = false;
            }
            return win;
    }

    public int checkCheoTrai() {
            int win = 0, cheot = 0, n = 0;
            boolean check = false;
            for (int i = 0; i < x; i++) {
                    for (int j = y - 1; j >= 0; j--) {
                            if (check) {
                                    if (matran[n - j - 2 * cheot][j] == 1) {
                                            cheot++;
                                            System.out.print("+" + j);
                                            if (cheot > 4) {
                                                    win = 1;
                                                    break;
                                            }
                                            continue;
                                    } else {
                                            check = false;
                                            cheot = 0;
                                    }
                            }
                            if (matran[i][j] == 1) {
                                    n = i + j;
                                    check = true;
                                    cheot++;
                            } else {
                                    check = false;
                            }
                    }
                    n = 0;
                    cheot = 0;
                    check = false;
            }
            return win;
    }
    //chat game
    
    public void caro(String x, String y,String player)
    {
        isPlaying=true;
        xx = Integer.parseInt(x);
        yy = Integer.parseInt(y);
        // danh dau vi tri danh
        //matran[xx][yy] = 1;
        matrandanh[xx][yy] = 1;
        bt[xx][yy].setEnabled(false);
//        bt[xx][yy].setIcon(oIcon);
//        bt[xx][yy].setDisabledIcon(oIcon);
        checkChosingTypeChess(bt[xx][yy],player);
        //bt[xx][yy].setBackground(Color.RED);
        
        // Kiem tra thang hay chua
        System.out.println("CheckH:" + checkHang());
        System.out.println("CheckC:" + checkCot());
        System.out.println("CheckCp:" + checkCheoPhai());
        System.out.println("CheckCt:" + checkCheoTrai());
        winner = (checkHang() == 1 || checkCot() == 1 || checkCheoPhai() == 1 || checkCheoTrai() == 1);
        
        
    }
    private void checkWin(){
        if (checkHang() == 1 || checkCot() == 1 || checkCheoPhai() == 1
                        || checkCheoTrai() == 1) {
                setEnableButton(false);
                thoigian.stop();
                try {
                        oos.writeObject("youlose,123,123,"+opponentName);
                } catch (IOException ex) 
                                              {
                }
                
                showMessageDialog("thắng");
                
        }
    }
    
    public static void main(String[] args) {
        LoginDialog loginDlg = new LoginDialog(f);
        loginDlg.setVisible(true);
        // if logon successfully
        if(loginDlg.isSucceeded() && !loginDlg.isCanceled()){
//            btnLogin.setText("Hi " + loginDlg.getUsername() + "!");
               new Client(loginDlg.getUsername());
        }
        else{
            System.exit(0);
        }
        //new Client();
        
    }

    @Override
    public void run() {
        try {
            running =true;
            while(running) {
                
//                oos.writeObject("login,"+userName);
//                oos.flush();
                
                
        
                
        }
        } catch (Exception e) {
            e.printStackTrace();
        }
        
    }
    private class Input implements Runnable{
        private ObjectInputStream in;
        //private String opponentName;

        public Input(ObjectInputStream in) {
            this.in=in;
        }

        @Override
        public void run() {
            while(true){
                try {
//                        String doithu =in.readUTF();
//                        opponentName =doithu;
//                        if(opponentName ==null){
//                            setEnableButton(false);
//                            send.setEnabled(false);
//                            break;
//                        }
//                        else{
//                            setEnableButton(true);
//                            send.setEnabled(true);
//                        }
                        //System.out.println(doithu);
                        String stream = in.readObject().toString();
                        String[] data = stream.split(",");
                        System.out.println(stream);
                        //if(data[0].equals(userName)){
//                        if(data[0].equals(content))
                            switch (data[0]) {
                                case "opponentName":
//                                    opponentName=data[1];
//                                    System.out.println(opponentName);
                                    send.setEnabled(true);
                                    rd_o.setEnabled(false);
                                    rd_x.setEnabled(false);
                                    int playFirst=Integer.parseInt(data[2]);
                                    if(playFirst==0){
                                        thoigian.start();
                                        setEnableButton(true);
                                        
                                        labelYou.setBorder(new MatteBorder(1,1,4,1,Color.BLACK));
                                        
                                    }
                                    else{
                                        labelOpponent.setBorder(new MatteBorder(1,1,4,1,Color.BLACK));
                                    }
                                    //newgame();
                                    opponentName=data[1];
                                    labelOpponent.setText(opponentName);
                                    System.out.println(opponentName);
                                    break;
                                case "chat":
                                    temp += data[3]+ ": " + data[1] + '\n';
                                    content.setText(temp);
                                    content.update(content.getGraphics());
                                    break;
                                case "caro":
                                    //System.out.println(data[3]);
                                    thoigian.start();
                                    THIRDTY_SECONDS =30000;
                                    opponentName = data[4];
                                    caro(data[1],data[2],opponentName);
                                    rd_o.setEnabled(false);
                                    rd_x.setEnabled(false);
                                    //checkWin();
                                    labelYou.setBorder(new MatteBorder(1,1,4,1,Color.BLACK));
                                    labelOpponent.setBorder(null);
                                    setEnableButton(true);
                                    send.setEnabled(true);
                                    
                                    break;
                                case "newgame":
                                    demthoigian.setText("00:30");
                                    newgame();
                                    break;
                                case "youlose":
                                    thoigian.stop();
                                    showMessageDialog("thua");
                                    setEnableButton(false);
                                    break;
                                case "youwin":
                                    thoigian.stop();
                                    
                                    setEnableButton(false);
                                    showMessageDialog("thắng");
                                    break;
                                case "exitroom":
                                case "exitgame":
                                    demthoigian.setText("00:30");
                                    JOptionPane.showMessageDialog(f, "Đối thủ đã thoát. Hãy tìm trận mới !");
                                    setEnableButton(false);
                                    send.setEnabled(false);
                                    labelOpponent.setText("Đối thủ");
                                    labelOpponent.setBorder(null);
                                    labelYou.setBorder(null);
                                    newgame();
                                    break;
                                
                                default:
                                    break;
                            }
                        //}
                } catch (Exception e) {

                    e.printStackTrace();
                    break;
                }
            }
        }
        
    }
  
    
    
}
