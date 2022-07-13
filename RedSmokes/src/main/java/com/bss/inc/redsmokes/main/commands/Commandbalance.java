package com.bss.inc.redsmokes.main.commands;

public class Commandbalance extends RedSmokesCommand {
    public Commandbalance() {
        super("balance");
    }

    @Override
    protected void run(final Server server, final CommandSource sender, final String commandLabel, final String[] args) throws Exception {
        if (args.length == 0) {
            throw new NotEnoughArgumentsException();
        }

        final User target = getPlayer(server, args, 0, false, true);
        sender.sendMessage(tl("balanceOther", target.isHidden() ? target.getName() : target.getDisplayName(), NumberUtil.displayCurrency(target.getMoney(), ess)));
    }

    @Override
    public void run(final Server server, final User user, final String commandLabel, final String[] args) throws Exception {
        if (args.length == 1 && user.isAuthorized("essentials.balance.others")) {
            final User target = getPlayer(server, args, 0, true, true);
            user.sendMessage(tl("balanceOther", target.isHidden() ? target.getName() : target.getDisplayName(), NumberUtil.displayCurrency(target.getMoney(), ess)));
        } else if (args.length < 2) {
            user.sendMessage(tl("balance", NumberUtil.displayCurrency(user.getMoney(), ess)));
        } else {
            throw new NotEnoughArgumentsException();
        }
    }

    @Override
    protected List<String> getTabCompleteOptions(final Server server, final CommandSource sender, final String commandLabel, final String[] args) {
        if (args.length == 1 && sender.isAuthorized("essentials.balance.others", ess)) {
            return getPlayers(server, sender);
        } else {
            return Collections.emptyList();
        }

    }
