package mage.cards.k;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

import mage.MageObject;
import mage.abilities.Ability;
import mage.abilities.common.LegendarySpellAbility;
import mage.abilities.effects.OneShotEffect;
import mage.cards.Card;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.cards.Cards;
import mage.cards.CardsImpl;
import mage.constants.CardType;
import mage.constants.ComparisonType;
import mage.constants.Outcome;
import mage.constants.SuperType;
import mage.constants.Zone;
import mage.filter.FilterCard;
import mage.filter.common.FilterPermanentCard;
import mage.filter.predicate.Predicates;
import mage.filter.predicate.mageobject.CardTypePredicate;
import mage.filter.predicate.mageobject.ConvertedManaCostPredicate;
import mage.filter.predicate.mageobject.SupertypePredicate;
import mage.game.Game;
import mage.players.Player;
import mage.target.TargetCard;

/**
 * @author JRHerlehy
 *         Created on 4/8/18.
 */
public class KamahlsDruidicVow extends CardImpl {

    public KamahlsDruidicVow(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.SORCERY}, "{X}{G}{G}");
        this.addSuperType(SuperType.LEGENDARY);

        // (You may cast a legendary sorcery only if you control a legendary creature or planeswalker.)
        this.addAbility(new LegendarySpellAbility());

        // Look at the top X cards of your library.
        // You may put any number of land and/or legendary permanent cards with converted mana cost X or less from among them onto the battlefield.
        // Put the rest into your graveyard.
        this.getSpellAbility().addEffect(new KamahlsDruidicVowEffect());
    }

    public KamahlsDruidicVow(final KamahlsDruidicVow card) {
        super(card);
    }

    @Override
    public KamahlsDruidicVow copy() {
        return new KamahlsDruidicVow(this);
    }

}

class KamahlsDruidicVowEffect extends OneShotEffect {

    public KamahlsDruidicVowEffect() {
        super(Outcome.PutCardInPlay);
        this.staticText = "Look at the top X cards of your library. You may put any number of land and/or legendary permanent cards with converted mana cost X or less from among them onto the battlefield. Put the rest into your graveyard";
    }

    public KamahlsDruidicVowEffect(final KamahlsDruidicVowEffect effect) {
        super(effect);
    }

    @Override
    public boolean apply(Game game, Ability source) {
        Player controller = game.getPlayer(source.getControllerId());
        MageObject sourceObject = game.getObject(source.getSourceId());
        if (controller == null || sourceObject == null) {
            return false;
        }

        Cards cards = new CardsImpl();
        int xValue = source.getManaCostsToPay().getX();
        int numCards = Math.min(controller.getLibrary().size(), xValue);
        for (int i = 0; i < numCards; i++) {
            Card card = controller.getLibrary().removeFromTop(game);
            cards.add(card);
        }
        if (!cards.isEmpty()) {
            FilterCard filter = new FilterPermanentCard("land and/or legendary permanent cards with converted mana cost " + xValue + " or less to put onto the battlefield");
            filter.add(new ConvertedManaCostPredicate(ComparisonType.FEWER_THAN, xValue + 1));
            filter.add(
                    Predicates.or(
                            new CardTypePredicate(CardType.LAND),
                            new SupertypePredicate(SuperType.LEGENDARY)
                    ));
            TargetCard target1 = new TargetCard(0, Integer.MAX_VALUE, Zone.LIBRARY, filter);
            target1.setRequired(false);

            controller.choose(Outcome.PutCardInPlay, cards, target1, game);
            Set<Card> toBattlefield = new LinkedHashSet<>();
            for (UUID cardId : target1.getTargets()) {
                Card card = cards.get(cardId, game);
                if (card != null) {
                    cards.remove(card);
                    toBattlefield.add(card);
                }
            }
            controller.moveCards(toBattlefield, Zone.BATTLEFIELD, source, game, false, false, false, null);
            controller.moveCards(cards, Zone.GRAVEYARD, source, game);
        }
        return true;
    }

    @Override
    public KamahlsDruidicVowEffect copy() {
        return new KamahlsDruidicVowEffect(this);
    }
}
