package cuchaz.enigma.gui.docker;

import java.awt.BorderLayout;
import java.awt.event.MouseEvent;
import java.util.Collection;
import java.util.Vector;

import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTree;
import javax.swing.ListSelectionModel;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

import cuchaz.enigma.analysis.ReferenceTreeNode;
import cuchaz.enigma.gui.Gui;
import cuchaz.enigma.gui.TokenListCellRenderer;
import cuchaz.enigma.gui.renderer.CallsTreeCellRenderer;
import cuchaz.enigma.gui.util.GuiUtil;
import cuchaz.enigma.gui.util.ScaleUtil;
import cuchaz.enigma.gui.util.SingleTreeSelectionModel;
import cuchaz.enigma.source.Token;
import cuchaz.enigma.translation.representation.entry.ClassEntry;
import cuchaz.enigma.translation.representation.entry.Entry;
import cuchaz.enigma.translation.representation.entry.FieldEntry;
import cuchaz.enigma.translation.representation.entry.MethodEntry;

public class CallsTreeDocker extends Docker {
	private final JTree tree = new JTree();
	private final JList<Token> tokens = new JList<>();

	public CallsTreeDocker(Gui gui) {
		super(gui);
		this.tree.setModel(null);
		this.tree.setCellRenderer(new CallsTreeCellRenderer(gui));
		this.tree.setSelectionModel(new SingleTreeSelectionModel());
		this.tree.setShowsRootHandles(true);
		this.tree.addMouseListener(GuiUtil.onMouseClick(this::onTreeClicked));

		this.tokens.setCellRenderer(new TokenListCellRenderer(gui.getController()));
		this.tokens.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		this.tokens.setLayoutOrientation(JList.VERTICAL);
		this.tokens.addMouseListener(GuiUtil.onMouseClick(this::onTokenClicked));
		this.tokens.setPreferredSize(ScaleUtil.getDimension(0, 200));
		this.tokens.setMinimumSize(ScaleUtil.getDimension(0, 200));

		JSplitPane contentPane = new JSplitPane(
				JSplitPane.VERTICAL_SPLIT,
				true,
				new JScrollPane(this.tree),
				new JScrollPane(this.tokens)
		);

		contentPane.setResizeWeight(1); // let the top side take all the slack
		contentPane.resetToPreferredSizes();
		this.add(contentPane, BorderLayout.CENTER);
	}

	public void showCalls(Entry<?> entry, boolean recurse) {
		TreeNode node = null;

		if (entry instanceof ClassEntry classEntry) {
			node = this.gui.getController().getClassReferences(classEntry);
		} else if (entry instanceof FieldEntry fieldEntry) {
			node = this.gui.getController().getFieldReferences(fieldEntry);
		} else if (entry instanceof MethodEntry methodEntry) {
			node = this.gui.getController().getMethodReferences(methodEntry, recurse);
		}

		this.tree.setModel(new DefaultTreeModel(node));

		this.setVisible(true);
	}

	public void showTokens(Collection<Token> tokens) {
		this.tokens.setListData(new Vector<>(tokens));
		this.tokens.setSelectedIndex(0);
	}

	public void clearTokens() {
		this.tokens.setListData(new Vector<>());
	}

	private void onTreeClicked(MouseEvent event) {
		if (event.getClickCount() >= 2 && event.getButton() == MouseEvent.BUTTON1) {
			// get the selected node
			TreePath path = this.tree.getSelectionPath();

			if (path == null) {
				return;
			}

			Object node = path.getLastPathComponent();

			if (node instanceof ReferenceTreeNode<?, ?> referenceNode) {
				if (referenceNode.getReference() != null) {
					this.gui.getController().navigateTo((Entry<?>) referenceNode.getReference());
				} else {
					this.gui.getController().navigateTo(referenceNode.getEntry());
				}
			}
		}
	}

	private void onTokenClicked(MouseEvent event) {
		if (event.getClickCount() == 2) {
			Token selected = this.tokens.getSelectedValue();
			if (selected != null) {
				this.gui.openClass(this.gui.getController().getTokenHandle().getRef()).navigateToToken(selected);
			}
		}
	}

	@Override
	public Location getButtonPosition() {
		return new Location(Side.RIGHT, VerticalLocation.TOP);
	}

	@Override
	public Location getPreferredLocation() {
		return new Location(Side.RIGHT, VerticalLocation.FULL);
	}

	@Override
	public String getId() {
		return "calls";
	}
}
