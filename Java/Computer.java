package test_4;

import java.awt.Color;
import java.awt.Font;
import java.awt.event.*;
import javax.swing.*;

public class Computer extends JFrame {

	Font f1 = new Font("问藏书房",Font.PLAIN,18);
    // 构造函数
    public Computer() {
    	
    	JFrame jf = new JFrame("计算器");
    	jf.setSize(300,400);

    	jf.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);//点击关闭时关闭程序
    	jf.setVisible(true);
    	jf.setResizable(false);
    	
    	JPanel panel = new JPanel();
    	jf.add(panel);
    	placeComponents(panel);
    	}
    
    	private void placeComponents(JPanel panel)
    	{
    		
        	panel.setBackground(new Color(175,238,238));
    		panel.setLayout(null);//自定义布局
    		
    		JLabel lb1 = new JLabel("第一个数");
    		panel.add(lb1);
    		lb1.setBounds(10,20,60,25);//setBounds(x, y, width, height) x 和 y 指定左上角的新位置，由 width 和 height 指定新的大小
    		lb1.setFont(f1);
    		
    		JLabel lb2 = new JLabel("第二个数");
    		panel.add(lb2);
    		lb2.setBounds(10,60,60,25);
    		lb2.setFont(f1);

    		JLabel lb3 = new JLabel("结果");
    		panel.add(lb3);
    		lb3.setBounds(10,100,60,25);
    		lb3.setFont(f1);
    		
    		JTextField number1 = new JTextField(20);
    		panel.add(number1);
    		number1.setBounds(100,20,100,25);

    		JTextField number2 = new JTextField(20);
    		panel.add(number2);
    		number2.setBounds(100,60,100,25);
    		
    		JTextField number3 = new JTextField(20);
    		panel.add(number3);
    		number3.setBounds(100,100,100,25);
    		
    		JButton b1 = new JButton("加");
    		panel.add(b1);
    		b1.setBounds(10,140,50,30);
    		b1.setFont(f1);
    		b1.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					// TODO 自动生成的方法存根
					String s1 = number1.getText();
					String s2 = number2.getText();
					Double num1 = Double.valueOf(s1);
					Double num2 = Double.valueOf(s2);
					String s3 = String.valueOf(num1+num2);
					number3.setText(s3);
				}
    		});
    		
    		JButton b2 = new JButton("减");
    		panel.add(b2);
    		b2.setBounds(80,140,50,30);
    		b2.setFont(f1);
    		b2.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					// TODO 自动生成的方法存根
					String s1 = number1.getText();
					String s2 = number2.getText();
					Double num1 = Double.valueOf(s1);
					Double num2 = Double.valueOf(s2);
					String s3 = String.valueOf(num1-num2);
					number3.setText(s3);
				}
    		});
    		
    		JButton b3 = new JButton("乘");
    		panel.add(b3);
    		b3.setBounds(150,140,50,30);
    		b3.setFont(f1);
    		b3.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					// TODO 自动生成的方法存根
					String s1 = number1.getText();
					String s2 = number2.getText();
					Double num1 = Double.valueOf(s1);
					Double num2 = Double.valueOf(s2);
					String s3 = String.valueOf(num1*num2);
					number3.setText(s3);
				}
    		});
    		
    		JButton b4 = new JButton("除");
    		panel.add(b4);
    		b4.setBounds(220,140,50,30);
    		b4.setFont(f1);
    		b4.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					// TODO 自动生成的方法存根
					String s1 = number1.getText();
					String s2 = number2.getText();
					Double num1 = Double.valueOf(s1);
					Double num2 = Double.valueOf(s2);
					String s3 = String.valueOf(num1/num2);
					number3.setText(s3);
				}
    		});
    		
    		
    		JButton b5 = new JButton("全部清零");
    		panel.add(b5);
    		b5.setBounds(20,190,240,30);
    		b5.setFont(f1);
    		b5.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					// TODO 自动生成的方法存根
					number1.setText("");
					number2.setText("");
					number3.setText("");
				}
    		});
    		
    	}

    
}
