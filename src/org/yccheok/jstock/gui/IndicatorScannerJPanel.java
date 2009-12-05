/*
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or (at
 * your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 *
 * Copyright (C) 2009 Yan Cheng CHEOK <yccheok@yahoo.com>
 */


package org.yccheok.jstock.gui;

import org.yccheok.jstock.alert.GoogleMail;
import org.yccheok.jstock.alert.GoogleCalendar;
import javax.mail.MessagingException;
import javax.mail.internet.AddressException;
import javax.swing.event.*;
import com.nexes.wizard.*;
import javax.swing.*;
import java.awt.event.*;
import java.io.File;
import javax.swing.table.*;
import java.util.*;
import org.yccheok.jstock.engine.*;
import org.yccheok.jstock.analysis.*;
import java.util.concurrent.*;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.yccheok.jstock.alert.SMSLimiter;
import org.yccheok.jstock.analysis.Indicator;
import org.yccheok.jstock.internationalization.GUIBundle;

/**
 *
 * @author  yccheok
 */
public class IndicatorScannerJPanel extends javax.swing.JPanel implements ChangeListener, org.yccheok.jstock.engine.Observer<Indicator, Boolean> {
    
    /** Creates new form IndicatorScannerJPanel */
    public IndicatorScannerJPanel() {
        initComponents();
        
        initTableHeaderToolTips();
        this.initGUIOptions();
        this.initAlertStateManager();
    }

    private void initAlertStateManager()
    {
        alertStateManager.clearState();
        alertStateManager.attach(this);
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        jButton1 = new javax.swing.JButton();
        jButton2 = new javax.swing.JButton();
        jPanel2 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTable1 = new javax.swing.JTable();

        setLayout(new java.awt.BorderLayout(5, 5));

        jButton1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/16x16/player_play.png"))); // NOI18N
        jButton1.setText("Scan...");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });
        jPanel1.add(jButton1);

        jButton2.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/16x16/stop.png"))); // NOI18N
        jButton2.setText("Stop");
        jButton2.setEnabled(false);
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton2ActionPerformed(evt);
            }
        });
        jPanel1.add(jButton2);

        add(jPanel1, java.awt.BorderLayout.SOUTH);

        jPanel2.setBorder(javax.swing.BorderFactory.createTitledBorder("Indicator Scan Result"));
        jPanel2.setLayout(new java.awt.BorderLayout());

        jTable1.setAutoCreateRowSorter(true);
        jTable1.setFont(new java.awt.Font("Tahoma", 1, 12));
        jTable1.setModel(new IndicatorTableModel());
        jTable1.setAutoResizeMode(javax.swing.JTable.AUTO_RESIZE_OFF);
        this.jTable1.setDefaultRenderer(Number.class, new StockTableCellRenderer());
        this.jTable1.setDefaultRenderer(Double.class, new StockTableCellRenderer());
        this.jTable1.setDefaultRenderer(Object.class, new StockTableCellRenderer());

        this.jTable1.getTableHeader().addMouseListener(new TableColumnSelectionPopupListener(2));
        this.jTable1.addMouseListener(new TableRowPopupListener());
        this.jTable1.addKeyListener(new TableKeyEventListener());
        jScrollPane1.setViewportView(jTable1);

        jPanel2.add(jScrollPane1, java.awt.BorderLayout.CENTER);

        add(jPanel2, java.awt.BorderLayout.CENTER);
    }// </editor-fold>//GEN-END:initComponents

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
// TODO add your handling code here:
        stop();
    }//GEN-LAST:event_jButton2ActionPerformed

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
// TODO add your handling code here:
        if (this.startScanThread != null)
        {
            try {
                this.startScanThread.join();
            } catch (InterruptedException ex) {
                log.error(null, ex);
            }

            this.startScanThread = null;
        }

        final MainFrame m = getMainFrame();
        
        if (m.getStockCodeAndSymbolDatabase() == null) {
            javax.swing.JOptionPane.showMessageDialog(this, "We haven't connected to stock server.", "Not Connected", javax.swing.JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        // Reset dirty flag, to allow background thread to show indicator on
        // the table.
        allowIndicatorShown = true;
        
        initWizardDialog();
        
        int ret = wizard.showModalDialog(680, -1);

        if (ret != Wizard.FINISH_RETURN_CODE)
            return;

        final WizardModel wizardModel = wizard.getModel();

        this.startScanThread = getStartScanThread(wizardModel, m);
        this.startScanThread.start();

        jButton1.setEnabled(false);
        jButton2.setEnabled(true);

        m.setStatusBar(true, "Indicator scanner is scanning...");
    }//GEN-LAST:event_jButton1ActionPerformed

    private void initGUIOptions() {        
        File f = new File(org.yccheok.jstock.gui.Utils.getUserDataDirectory() + "config" + File.separator + "indicatorscannerjpanel.xml");
        GUIOptions guiOptions = org.yccheok.jstock.gui.Utils.fromXML(GUIOptions.class, f);

        if (guiOptions == null)
        {
            return;
        }

        if (guiOptions.getJTableOptionsSize() <= 0)
        {
            return;
        }

        /* Set Table Settings */
        JTableUtilities.setJTableOptions(jTable1,guiOptions.getJTableOptions(0));
	}

    public boolean saveGUIOptions() {
        if(Utils.createCompleteDirectoryHierarchyIfDoesNotExist(org.yccheok.jstock.gui.Utils.getUserDataDirectory() + "config") == false)
        {
            return false;
        }

        final GUIOptions.JTableOptions jTableOptions = new GUIOptions.JTableOptions();

        final int count = this.jTable1.getColumnCount();
        for (int i = 0; i < count; i++) {
            final String name = this.jTable1.getColumnName(i);
            final TableColumn column = jTable1.getColumnModel().getColumn(i);
            jTableOptions.addColumnOption(GUIOptions.JTableOptions.ColumnOption.newInstance(name, column.getWidth()));
        }

        final GUIOptions guiOptions = new GUIOptions();
        guiOptions.addJTableOptions(jTableOptions);

        File f = new File(org.yccheok.jstock.gui.Utils.getUserDataDirectory() + "config" + File.separator + "indicatorscannerjpanel.xml");
        return org.yccheok.jstock.gui.Utils.toXML(guiOptions, f);
    }

    // Time consuming method. It involves file I/O reading (getOperatorIndicator).
    private void initOperatorIndicators(WizardModel wizardModel)
    {
        this.operatorIndicators.clear();

        WizardPanelDescriptor wizardPanelDescriptor0 = wizardModel.getPanelDescriptor(WizardSelectStockDescriptor.IDENTIFIER);
        WizardSelectStockJPanel wizardSelectStockJPanel = (WizardSelectStockJPanel)wizardPanelDescriptor0.getPanelComponent();
        WizardPanelDescriptor wizardPanelDescriptor1 = wizardModel.getPanelDescriptor(WizardSelectIndicatorDescriptor.IDENTIFIER);
        WizardSelectIndicatorJPanel wizardSelectIndicatorJPanel = (WizardSelectIndicatorJPanel)wizardPanelDescriptor1.getPanelComponent();

        final MainFrame m = getMainFrame();
        final StockCodeAndSymbolDatabase stockCodeAndSymbolDatabase = m.getStockCodeAndSymbolDatabase();
        final IndicatorProjectManager alertIndicatorProjectManager = m.getAlertIndicatorProjectManager();
        java.util.List<String> projects = wizardSelectIndicatorJPanel.getSelectedProjects();
        java.util.List<Code> codes = wizardSelectStockJPanel.getSelectedCodes();

        for (final Code code : codes) {
            final java.util.List<OperatorIndicator> result = new java.util.ArrayList<OperatorIndicator>();

            operatorIndicators.put(code, result);

            for (String project : projects) {
                final OperatorIndicator operatorIndicator = alertIndicatorProjectManager.getOperatorIndicator(project);

                if(operatorIndicator != null) {
                    final Stock stock = Utils.getEmptyStock(code, stockCodeAndSymbolDatabase.codeToSymbol(code));

                    operatorIndicator.setStock(stock);

                    result.add(operatorIndicator);
                }
            }   /* for(String project : projects) */
        }   /* for(String code : codes) */
    }

    private void submitOperatorIndicatorToMonitor()
    {
        final Set<Code> codes = operatorIndicators.keySet();
        Duration historyDuration = Duration.getTodayDurationByDays(0);

        for(Code code : codes) {
            final List<OperatorIndicator> operatorIndicatos = operatorIndicators.get(code);

            for (OperatorIndicator operatorIndicator : operatorIndicatos)
            {
                historyDuration = historyDuration.getUnionDuration(operatorIndicator.getNeededStockHistoryDuration());
            }

            // Dirty way to speed up calculation.
            // Currently, all codes are having same set of indicators,
            // and duration is independent of code type.
            break;
        }

        // Duration must be initialized, before codes being added.
        this.stockHistoryMonitor.setDuration(historyDuration);

        for (Code code : codes) {
            final List<OperatorIndicator> operatorIndicatos = operatorIndicators.get(code);

            boolean done = true;
            for (OperatorIndicator operatorIndicator : operatorIndicatos)
            {
                if (operatorIndicator.isStockHistoryCalculationDone() == false)
                {
                    done = false;
                    
                    // Early break. We will let history monitor to perform pre-calculation.
                    break;
                }
                else
                {
                    operatorIndicator.preCalculate();
                }
            }

            if (done)
            {
                // Perform real time monitoring, for the code with history information.
                realTimeStockMonitor.addStockCode(code);
            }
            else
            {
                this.stockHistoryMonitor.addStockCode(code);
            }
        }
    }

    @Override
    public void update(final Indicator indicator, Boolean result) {
        final boolean flag = result;

        if (flag == false)
        {
            removeIndicatorFromTable(indicator);
            return;
        }

        addIndicatorToTable(indicator);

        final MainFrame m = MainFrame.getInstance();
        final JStockOptions jStockOptions = m.getJStockOptions();

        if (jStockOptions.isPopupMessage()) {
            final Runnable r = new Runnable() {
                @Override
                public void run() {
                    final Stock stock = indicator.getStock();
                    final double price = stock.getLastPrice();
                    final String message = stock.getSymbol() + " (" + price + ") hits " + indicator.toString();

                    if (jStockOptions.isPopupMessage()) {
                        m.displayPopupMessage(stock.getSymbol().toString(), message);

                        try {
                            Thread.sleep(jStockOptions.getAlertSpeed() * 1000);
                        }
                        catch(InterruptedException exp) {
                            log.error("", exp);
                        }
                    }
                }
            };

            try {
                systemTrayAlertPool.submit(r);
            }
            catch(java.util.concurrent.RejectedExecutionException exp) {
                log.error("", exp);
            }
        }

        if (jStockOptions.isSendEmail()) {
            final Runnable r = new Runnable() {
                @Override
                public void run() {
                    final Stock stock = indicator.getStock();
					final double price = stock.getLastPrice();
                    final String title = stock.getSymbol() + " (" + price + ") hits " + indicator.toString();

                    final String message = title + "\n(JStock)";

                    try {
                        String email = Utils.decrypt(jStockOptions.getEmail());
                        GoogleMail.Send(email, Utils.decrypt(jStockOptions.getEmailPassword()), email + "@gmail.com", title, message);
                    } catch (AddressException exp) {
                        log.error("", exp);
                    } catch (MessagingException exp) {
                        log.error("", exp);
                    }
                }
            };

            try {
                emailAlertPool.submit(r);
            }
            catch(java.util.concurrent.RejectedExecutionException exp) {
                log.error("", exp);
            }
        }

        if(jStockOptions.isSMSEnabled()) {
            final Runnable r = new Runnable() {
                @Override
                public void run() {
                    final Stock stock = indicator.getStock();
					final double price = stock.getLastPrice();
                    final String message = stock.getSymbol() + " (" + price + ") hits " + indicator.toString();

                    final String username = Utils.decrypt(jStockOptions.getGoogleCalendarUsername());
                    if (SMSLimiter.INSTANCE.isSMSAllowed()) {
                        final boolean status = GoogleCalendar.SMS(username, Utils.decrypt(jStockOptions.getGoogleCalendarPassword()), message);
                        if (status) {
                            SMSLimiter.INSTANCE.inc();
                        }
                    }
                }
            };

            try {
                smsAlertPool.submit(r);
            }
            catch(java.util.concurrent.RejectedExecutionException exp) {
                log.error(null, exp);
            }
        }
    }

    private static class ColumnHeaderToolTips extends MouseMotionAdapter {

        // Current column whose tooltip is being displayed.
        // This variable is used to minimize the calls to setToolTipText().
        TableColumn curCol;
    
        // Maps TableColumn objects to tooltips
        Map<TableColumn, String> tips = new HashMap<TableColumn, String>();
    
        // If tooltip is null, removes any tooltip text.
        public void setToolTip(TableColumn col, String tooltip) {
            if (tooltip == null) {
                tips.remove(col);
            } else {
                tips.put(col, tooltip);
            }
        }
    
        public void mouseMoved(MouseEvent evt) {
            TableColumn col = null;
            JTableHeader header = (JTableHeader)evt.getSource();
            JTable table = header.getTable();
            TableColumnModel colModel = table.getColumnModel();
            int vColIndex = colModel.getColumnIndexAtX(evt.getX());
    
            // Return if not clicked on any column header
            if (vColIndex >= 0) {
                col = colModel.getColumn(vColIndex);
            }
    
            if (col != curCol) {
                header.setToolTipText((String)tips.get(col));
                curCol = col;
            }
        }
    }
    
    private void initTableHeaderToolTips() {
        JTableHeader header = jTable1.getTableHeader();
    
        ColumnHeaderToolTips tips = new ColumnHeaderToolTips();
            
        header.addMouseMotionListener(tips);        
    }
    
    @Override
    public void stateChanged(javax.swing.event.ChangeEvent evt) {
    }    
    
    public void clear()
    {
        final MainFrame m = getMainFrame();
        this.initRealTimeStockMonitor(m.getStockServerFactories());
        this.initStockHistoryMonitor(m.getStockServerFactories());

        this.operatorIndicators.clear();

        // Ask help from dirty flag, so that background thread won't have
        // chance to show indicators on the table.
        allowIndicatorShown = false;
        
        removeAllIndicatorsFromTable();

        m.setStatusBar(false, "Connected");
    }
    
    public void stop()
    {
        if (this.startScanThread != null)
        {
            try {
                this.startScanThread.join();
            } catch (InterruptedException ex) {
                log.error(null, ex);
            }

            this.startScanThread = null;
        }
        
        final MainFrame m = getMainFrame();
        this.initRealTimeStockMonitor(m.getStockServerFactories());
        this.initStockHistoryMonitor(m.getStockServerFactories());

        final ExecutorService oldSystemTrayAlertPool = systemTrayAlertPool;
        final ExecutorService oldEmailAlertPool = emailAlertPool;
        final ExecutorService oldSMSAlertPool = smsAlertPool;

        Utils.getZoombiePool().execute(new Runnable() {
            @Override
            public void run() {
                log.info("Prepare to shut down " + oldSystemTrayAlertPool + "...");
                oldSystemTrayAlertPool.shutdownNow();
                try {            
                    oldSystemTrayAlertPool.awaitTermination(100, TimeUnit.DAYS);
                } catch (InterruptedException exp) {
                    log.error(null, exp);
                }
                log.info("Shut down " + oldSystemTrayAlertPool + " peacefully.");
                
                log.info("Prepare to shut down " + oldEmailAlertPool + "...");
                oldEmailAlertPool.shutdownNow();
                try {            
                    oldEmailAlertPool.awaitTermination(100, TimeUnit.DAYS);
                } catch (InterruptedException exp) {
                    log.error(null, exp);
                }
                log.info("Shut down " + oldEmailAlertPool + " peacefully.");

                log.info("Prepare to shut down " + oldSMSAlertPool + "...");
                oldSMSAlertPool.shutdownNow();
                try {
                    oldSMSAlertPool.awaitTermination(100, TimeUnit.DAYS);
                } catch (InterruptedException exp) {
                    log.error(null, exp);
                }
                log.info("Shut down " + oldSMSAlertPool + " peacefully.");
            }
        }); 
        
        emailAlertPool = Executors.newFixedThreadPool(1);
        smsAlertPool = Executors.newFixedThreadPool(1);
        systemTrayAlertPool = Executors.newFixedThreadPool(1);        
        
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                jButton1.setEnabled(true);
                jButton2.setEnabled(false);            
            }
            
        });

        m.setStatusBar(false, "Connected");
    }
    
    private void initWizardDialog() {
        final MainFrame m = getMainFrame();
        
        wizard = new Wizard(m);

        wizard.getDialog().setTitle("Indicator Scanning Wizard");
        wizard.getDialog().setResizable(false);
        
        WizardPanelDescriptor wizardSelectIndicatorDescriptor = new WizardSelectIndicatorDescriptor();
        wizard.registerWizardPanel(WizardSelectIndicatorDescriptor.IDENTIFIER, wizardSelectIndicatorDescriptor);
        
        // Quick hack. WizardSelectStockJPanel has no way to obtain MainFrame, during its construction
        // stage.
        WizardPanelDescriptor wizardSelectStockDescriptor = new WizardSelectStockDescriptor(m.getStockCodeAndSymbolDatabase());
        wizard.registerWizardPanel(WizardSelectStockDescriptor.IDENTIFIER, wizardSelectStockDescriptor);
        
        wizard.setCurrentPanel(WizardSelectIndicatorDescriptor.IDENTIFIER); 
 
        // Center to screen.
        wizard.getDialog().setLocationRelativeTo(null);
    }
    
    public void updateScanningSpeed(int speed) {
        this.realTimeStockMonitor.setDelay(speed);
    }

    public void initStockHistoryMonitor(java.util.List<StockServerFactory> stockServerFactories) {
        if (stockHistoryMonitor != null) {
            final StockHistoryMonitor oldStockHistoryMonitor = stockHistoryMonitor;
            Utils.getZoombiePool().execute(new Runnable() {
                @Override
                public void run() {
                    log.info("Prepare to shut down " + oldStockHistoryMonitor + "...");
                    oldStockHistoryMonitor.clearStockCodes();
                    oldStockHistoryMonitor.dettachAll();
                    oldStockHistoryMonitor.stop();
                    log.info("Shut down " + oldStockHistoryMonitor + " peacefully.");
                }
            });
        }

        this.stockHistoryMonitor = new StockHistoryMonitor(NUM_OF_THREADS_HISTORY_MONITOR);
        stockHistoryMonitor.setStockServerFactories(stockServerFactories);

        stockHistoryMonitor.attach(stockHistoryMonitorObserver);

        // No StockHistorySerializer at this moment, either read or write. This is because
        // (1) Read - If the duration of the history selected in Real-Time panel is shorter than
        // indicators's, we will be in trouble.
        // (2) Write - If the duration of the indicator's is shorter than Real-Time panel, we will
        // be in trouble again.
        //
        // Currently, we have no way but disable it.
    }

    private void update(StockHistoryMonitor monitor, StockHistoryMonitor.StockHistoryRunnable runnable)
    {
        final MainFrame m = this.getMainFrame();
        final Code code = runnable.getCode();

        List<OperatorIndicator> indicators = this.operatorIndicators.get(code);
        if (indicators == null)
        {
            return;
        }

        final StockHistoryServer stockHistoryServer = runnable.getStockHistoryServer();
        if (stockHistoryServer == null)
        {
            // Probably the network is down. Retry infinityly.
            monitor.addStockCode(code);
            return;
        }

        Symbol symbol = m.getStockCodeAndSymbolDatabase().codeToSymbol(code);

        m.setStatusBar(true, "Indicator scanner found " + symbol + " history. Perform calculation...");

        for (OperatorIndicator operatorIndicator : indicators)
        {
            if (operatorIndicator.isStockHistoryServerNeeded())
            {
                operatorIndicator.setStockHistoryServer(stockHistoryServer);
            }

            operatorIndicator.preCalculate();
        }

        // Perform real time monitoring, for the code with history information.
        realTimeStockMonitor.addStockCode(code);
    }

    private org.yccheok.jstock.engine.Observer<StockHistoryMonitor, StockHistoryMonitor.StockHistoryRunnable> getStockHistoryMonitorObserver() {
        return new org.yccheok.jstock.engine.Observer<StockHistoryMonitor, StockHistoryMonitor.StockHistoryRunnable>() {
            @Override
            public void update(StockHistoryMonitor monitor, StockHistoryMonitor.StockHistoryRunnable runnable)
            {
                IndicatorScannerJPanel.this.update(monitor, runnable);
            }
        };
    }

    public void updatePrimaryStockServerFactory(java.util.List<StockServerFactory> stockServerFactories) {
        if (realTimeStockMonitor != null) {
            realTimeStockMonitor.setStockServerFactories(stockServerFactories);
        }

        if (stockHistoryMonitor != null) {
            stockHistoryMonitor.setStockServerFactories(stockServerFactories);
        }
    }

    public void initRealTimeStockMonitor(java.util.List<StockServerFactory> stockServerFactories) {
        if (realTimeStockMonitor != null) {
            final RealTimeStockMonitor oldRealTimeStockMonitor = realTimeStockMonitor;
            Utils.getZoombiePool().execute(new Runnable() {
                @Override
                public void run() {
                    log.info("Prepare to shut down " + oldRealTimeStockMonitor + "...");
                    oldRealTimeStockMonitor.clearStockCodes();
                    oldRealTimeStockMonitor.dettachAll();
                    oldRealTimeStockMonitor.stop();
                    log.info("Shut down " + oldRealTimeStockMonitor + " peacefully.");
                }
            });
        }
        
        realTimeStockMonitor = new RealTimeStockMonitor(4, 20, MainFrame.getInstance().getJStockOptions().getScanningSpeed());
        realTimeStockMonitor.setStockServerFactories(stockServerFactories);
        
        realTimeStockMonitor.attach(this.realTimeStockMonitorObserver);
    }
    
    // This is the workaround to overcome Erasure by generics. We are unable to make MainFrame to
    // two observers at the same time.
    private org.yccheok.jstock.engine.Observer<RealTimeStockMonitor, java.util.List<Stock>> getRealTimeStockMonitorObserver() {
        return new org.yccheok.jstock.engine.Observer<RealTimeStockMonitor, java.util.List<Stock>>() {
            @Override
            public void update(RealTimeStockMonitor monitor, java.util.List<Stock> stocks)
            {
                IndicatorScannerJPanel.this.update(monitor, stocks);
            }
        };
    }
    
    public void update(RealTimeStockMonitor monitor, final java.util.List<Stock> stocks) {
        final MainFrame m = this.getMainFrame();

        if (stocks.size() > 0)
        {
            m.setStatusBar(true, "Indicator scanner is scanning " + stocks.get(0).getSymbol() +"...");
        }

        for(Stock stock : stocks) {
            final java.util.List<OperatorIndicator> indicators = operatorIndicators.get(stock.getCode());
            
            if(indicators == null) continue;
            
            final JStockOptions jStockOptions = MainFrame.getInstance().getJStockOptions();

            if(jStockOptions.isSingleIndicatorAlert()) {
                for(OperatorIndicator indicator : indicators) {
                    indicator.setStock(stock);
                    alertStateManager.alert(indicator);
                }
            }
            else
            {
                // Multiple indicators alert.
                for(OperatorIndicator indicator : indicators) {
                    indicator.setStock(stock);
                }

                alertStateManager.alert(indicators);
            }
        }                
    }  
    
    // Should we synchronized the jTable1, or post the job at GUI event dispatch
    // queue?
    private void addIndicatorToTable(final Indicator indicator) {
        final Runnable r = new Runnable() {
            @Override
            public void run() {          
                IndicatorTableModel tableModel = (IndicatorTableModel)jTable1.getModel();
                
                // Dirty way to prevent background thread from showing indicators
                // on the table.
                if(allowIndicatorShown)
                    tableModel.addIndicator(indicator);
           } 
        };
        
        SwingUtilities.invokeLater(r);
    }
    
    private void removeIndicatorFromTable(final Indicator indicator) {
        final Runnable r = new Runnable() {
            @Override
            public void run() {          
                IndicatorTableModel tableModel = (IndicatorTableModel)jTable1.getModel();
                tableModel.removeIndicator(indicator);
           } 
        };
        
        SwingUtilities.invokeLater(r);        
    }
    
    private void removeAllIndicatorsFromTable() {
        final Runnable r = new Runnable() {
            @Override
            public void run() {          
                IndicatorTableModel tableModel = (IndicatorTableModel)jTable1.getModel();
                tableModel.removeAll();
           } 
        };
        
        SwingUtilities.invokeLater(r);        
    }
    
    private class TableRowPopupListener extends MouseAdapter {
        
        @Override
        public void mouseClicked(MouseEvent evt) {
        }
        
        @Override
        public void mousePressed(MouseEvent e) {
            maybeShowPopup(e);
        }

        @Override
        public void mouseReleased(MouseEvent e) {
            maybeShowPopup(e);
        }

        private void maybeShowPopup(MouseEvent e) {
            if (e.isPopupTrigger()) {
                if(jTable1.getSelectedColumn() != -1)
                    getMyJTablePopupMenu().show(e.getComponent(), e.getX(), e.getY());
            }
        }
    }

    private ImageIcon getImageIcon(String imageIcon) {
        return new javax.swing.ImageIcon(getClass().getResource(imageIcon));
    }   

    private JPopupMenu getMyJTablePopupMenu() {
        JPopupMenu popup = new JPopupMenu();
        
        final MainFrame m = getMainFrame();
        
        javax.swing.JMenuItem menuItem = new JMenuItem("History...", this.getImageIcon("/images/16x16/strokedocker.png"));
        
        menuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                int rows[] = jTable1.getSelectedRows();
                final IndicatorTableModel tableModel = (IndicatorTableModel)jTable1.getModel();

                for(int row : rows) {
                    final int modelIndex = jTable1.convertRowIndexToModel(row);
                    final Indicator indicator = tableModel.getIndicator(modelIndex);
                    if(indicator != null)
                        m.displayHistoryChart(indicator.getStock());
                }
            }
        });

        popup.add(menuItem);

        if (jTable1.getSelectedRowCount() == 1) {
            popup.addSeparator();

            menuItem = new JMenuItem(GUIBundle.getString("IndicatorScannerJPanel_Buy..."), this.getImageIcon("/images/16x16/inbox.png"));

            menuItem.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent evt) {
                    final int row = jTable1.getSelectedRow();
                    final int modelIndex = jTable1.getRowSorter().convertRowIndexToModel(row);
                    final IndicatorTableModel tableModel = (IndicatorTableModel)jTable1.getModel();
                    final Indicator indicator = tableModel.getIndicator(modelIndex);
                    final Stock stock = indicator.getStock();
                    MainFrame.getInstance().getPortfolioManagementJPanel().showNewBuyTransactionJDialog(stock, stock.getLastPrice(), false);
                }
            });

            popup.add(menuItem);
        }

        return popup;
    }

    private MainFrame getMainFrame()
    {
        if (mainFrame == null)
        {
            mainFrame = MainFrame.getInstance();
        }

        return mainFrame;
    }

    public void repaintTable() {
        jTable1.repaint();
    }
    
    public void clearTableSelection() {
        jTable1.getSelectionModel().clearSelection();
    }

    public boolean saveAsCSVFile(File file) {
        final TableModel tableModel = jTable1.getModel();
        // Unexpected result may happen while scanning is running, as table
        // model will be mutated during the middle of writting. Currently, we
        // do not have solution.
        final org.yccheok.jstock.file.Statements statements = org.yccheok.jstock.file.Statements.newInstanceFromTableModel(tableModel);
        if (statements == null) {
            return false;
        }
        return statements.saveAsCSVFile(file);
    }

    public boolean saveAsExcelFile(File file) {
        final TableModel tableModel = jTable1.getModel();
        // Unexpected result may happen while scanning is running, as table
        // model will be mutated during the middle of writting. Currently, we
        // do not have solution.
        final org.yccheok.jstock.file.Statements statements = org.yccheok.jstock.file.Statements.newInstanceFromTableModel(tableModel);
        if (statements == null) {
            return false;
        }
        return statements.saveAsExcelFile(file, GUIBundle.getString("IndicatorScannerJPanel_Title"));
    }

    private Thread getStartScanThread(final WizardModel wizardModel, final MainFrame mainFrame)
    {
        return new Thread(new Runnable() {
            @Override
            public void run() {
                WizardPanelDescriptor wizardPanelDescriptor0 = wizardModel.getPanelDescriptor(WizardSelectStockDescriptor.IDENTIFIER);
                WizardSelectStockJPanel wizardSelectStockJPanel = (WizardSelectStockJPanel)wizardPanelDescriptor0.getPanelComponent();

                if (wizardSelectStockJPanel.buildSelectedCode() == false) {
                    // Unlikely.
                    log.error("Fail to build selected stock");
                    return;
                }

                initRealTimeStockMonitor(mainFrame.getStockServerFactories());
                initStockHistoryMonitor(mainFrame.getStockServerFactories());

                removeAllIndicatorsFromTable();
                alertStateManager.clearState();

                initOperatorIndicators(wizardModel);
                submitOperatorIndicatorToMonitor();
            }
        });
    }

    private class TableKeyEventListener extends java.awt.event.KeyAdapter {
        @Override
        public void keyTyped(java.awt.event.KeyEvent e) {
            IndicatorScannerJPanel.this.clearTableSelection();
        }
    }
    
    private Wizard wizard;
    private RealTimeStockMonitor realTimeStockMonitor;
    private org.yccheok.jstock.engine.Observer<RealTimeStockMonitor, java.util.List<Stock>> realTimeStockMonitorObserver = this.getRealTimeStockMonitorObserver();
    private java.util.Map<Code, java.util.List<OperatorIndicator>> operatorIndicators = new java.util.concurrent.ConcurrentHashMap<Code, java.util.List<OperatorIndicator>>();

    private final AlertStateManager alertStateManager = new AlertStateManager();
    private ExecutorService emailAlertPool = Executors.newFixedThreadPool(1);
    private ExecutorService smsAlertPool = Executors.newFixedThreadPool(1);
    private ExecutorService systemTrayAlertPool = Executors.newFixedThreadPool(1);
    private MainFrame mainFrame = null;
    
    private org.yccheok.jstock.engine.Observer<StockHistoryMonitor, StockHistoryMonitor.StockHistoryRunnable> stockHistoryMonitorObserver = this.getStockHistoryMonitorObserver();

    private StockHistoryMonitor stockHistoryMonitor = null;

    // Dirty flag to be used with clear method and start button method.
    // Ensure we have an instant way to prevent background thread from showing
    // indicators on the table, after we call clear method. 
	// This is a dirty way, but it just work :)
    private volatile Boolean allowIndicatorShown = true;

    private Thread startScanThread = null;

    private static final Log log = LogFactory.getLog(IndicatorScannerJPanel.class);

    private static final int NUM_OF_THREADS_HISTORY_MONITOR = 4;

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTable jTable1;
    // End of variables declaration//GEN-END:variables
    
}
