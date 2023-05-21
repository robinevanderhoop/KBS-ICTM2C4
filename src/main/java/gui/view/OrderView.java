package gui.view;

import constants.Constants;
import database.dao.OrderDao;
import database.model.Order;
import database.model.OrderLine;
import database.util.DatabaseConnection;
import database.util.RowLockType;
import gui.ViewBuilder;
import gui.controller.OrderController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class OrderView extends JPanel implements ViewBuilder {

    private static final Logger logger = LoggerFactory.getLogger(OrderView.class);

    private final NavbarView navbarView;
    private final OrderDao orderDao;
    private final OrderController orderController;

    ListSelectionEvent e;

    public OrderView(CardLayout layout, JPanel root) {
        this.navbarView = new NavbarView(layout, root);
        this.orderDao = OrderDao.getInstance();
        this.orderController = new OrderController(layout, root);
        buildAndShowView();
    }

    @Override
    public void buildAndShowView() {
        this.setLayout(new BorderLayout());

        this.add(navbarView, BorderLayout.NORTH);

        List<Order> allOrders = new ArrayList<>();

        try (Connection con = DatabaseConnection.getConnection()) {
            allOrders.addAll(this.orderDao.getAllOrders(con));
        } catch (SQLException e) {
            e.printStackTrace();
            logger.error(e.getMessage());
        }

        JPanel singleOrder = new JPanel();
        singleOrder.setBackground(Color.WHITE);

        JList<Order> orderList = new JList<>(allOrders.toArray(new Order[0]));
        orderList.addListSelectionListener(e -> orderController.listSelected(e, orderList, singleOrder));
        orderList.setSelectionBackground(Color.GRAY);
        JScrollPane scrollPane = new JScrollPane(orderList);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);

        JPanel centralPanel = new JPanel();
        centralPanel.setLayout(new GridLayout(1, 2));
        centralPanel.add(scrollPane);
        this.add(centralPanel, BorderLayout.CENTER);
        centralPanel.add(singleOrder);

        JPanel orderBottomBarButtons = new JPanel();
        orderBottomBarButtons.setLayout(new FlowLayout(FlowLayout.LEFT));

        JPanel orderBottomBar = new JPanel();
        orderBottomBar.setLayout(new GridLayout(1, 2));
        orderBottomBar.add(orderBottomBarButtons);
        orderBottomBar.setSize(Constants.SCREEN_WIDTH, Constants.SCREEN_HEIGHT / 10);

        JButton addOrder = new JButton("Toevoegen");
        addOrder.addActionListener(orderController::addButton);
        orderBottomBarButtons.add(addOrder);

        JButton editOrder = new JButton("Bewerken");
        editOrder.addActionListener(orderController::editButton);
        orderBottomBarButtons.add(editOrder);

        JLabel searchOrder = new JLabel("Zoeken:");
        JTextField searchOrderTextField = new JTextField();
        searchOrderTextField.setPreferredSize(new Dimension(Constants.SCREEN_WIDTH / 20, Constants.SCREEN_HEIGHT / 27));
        searchOrderTextField.addActionListener((e) -> orderController.searchTextField(e, orderList, allOrders));
        orderBottomBarButtons.add(searchOrder);
        orderBottomBarButtons.add(searchOrderTextField);

        JLabel totalOrders = new JLabel(String.format("Totaal aantal orders: %d", allOrders.size()));

        JPanel orderBottomBarText = new JPanel();
        orderBottomBarText.setLayout(new FlowLayout(FlowLayout.RIGHT));
        orderBottomBarText.add(totalOrders);

        orderBottomBar.add(orderBottomBarText);

        this.add(orderBottomBar, BorderLayout.SOUTH);

        this.setVisible(true);
    }
}
